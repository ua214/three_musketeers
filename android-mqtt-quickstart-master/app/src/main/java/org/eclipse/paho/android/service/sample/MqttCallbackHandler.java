/*******************************************************************************
 * Copyright (c) 1999, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.eclipse.paho.android.service.sample;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.sample.Connection.ConnectionStatus;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;


/**
 * Handles call backs from the MQTT Client
 *
 */
public class MqttCallbackHandler extends ListActivity implements MqttCallback , LocationListener {

  /** {@link Context} for the application used to format and import external strings**/
  private Context context;
  /** Client handle to reference the connection that this handler is attached to**/
  private String clientHandle;


  LocationManager locationManager;
  String mprovider;

  String Current_latitude =null;
  String Current_longitude = null;
  /**
   * Creates an <code>MqttCallbackHandler</code> object
   * @param context The application's context
   * @param clientHandle The handle to a {@link Connection} object
   */
  public MqttCallbackHandler(Context context, String clientHandle)
  {
    this.context = context;
    this.clientHandle = clientHandle;
  }

  /**
   * @see org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(java.lang.Throwable)
   */
  @Override
  public void connectionLost(Throwable cause) {
//	  cause.printStackTrace();
    if (cause != null) {
      Connection c = Connections.getInstance(context).getConnection(clientHandle);
      c.addAction("Connection Lost");
      c.changeConnectionStatus(ConnectionStatus.DISCONNECTED);

      //format string to use a notification text
      Object[] args = new Object[2];
      args[0] = c.getId();
      args[1] = c.getHostName();

      String message = context.getString(R.string.connection_lost, args);

      //build intent
      Intent intent = new Intent();
      intent.setClassName(context, "org.eclipse.paho.android.service.sample.ConnectionDetails");
      intent.putExtra("handle", clientHandle);

      //notify the user
      Notify.notifcation(context, message, intent, R.string.notifyTitle_connectionLost);
    }
  }
  int i =0;
  /**
   * @see org.eclipse.paho.client.mqttv3.MqttCallback#messageArrived(java.lang.String, org.eclipse.paho.client.mqttv3.MqttMessage)
   */
  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {




    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    Criteria criteria = new Criteria();

    mprovider = locationManager.getBestProvider(criteria, false);


    Location location = locationManager.getLastKnownLocation(mprovider);
    locationManager.requestLocationUpdates(mprovider, 15000, 1, this);

    if (location != null)
      onLocationChanged(location);
    else
      Toast.makeText(getBaseContext(), "No Location Provider Found Check Your Code", Toast.LENGTH_SHORT).show();




    //Get connection object associated with this object
    Connection c = Connections.getInstance(context).getConnection(clientHandle);

    //create arguments to format message arrived notifcation string
    String[] args = new String[2];
    args[0] = new String(message.getPayload());
    args[1] = topic+";qos:"+message.getQos()+";retained:"+message.isRetained();
    Log.v("TAG", "index=" + args[0]);
    String smscheck = args[0].substring(2,args[0].indexOf(':')-1);
    Log.v("TAG", "smscheck=" + smscheck);

    if(smscheck.equals("Voltage")){
      Log.v("TAG", "SMS=" + "SMSSent");

      Vibrator v = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
      // Vibrate for 500 milliseconds
      v.vibrate(500);
      i++;
      String phoneNumber = "+447598821795";
      String mssg ="Help ";
      try{
         mssg = "Heeeellllpp " + ClientConnections.latitude + " " + ClientConnections.longitiude;

      }catch (Exception e) {

        e.printStackTrace();
      }

      try{
        mssg = "Heeeellllpp " + Current_latitude + " " + Current_longitude;

      }catch (Exception e) {

        e.printStackTrace();
      }


      try {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, mssg, null, null);

      } catch (Exception e) {

        e.printStackTrace();
      }


    }else{

      String phoneNumber = "+447598821795";
      String mssg = "The person has stopped bending  ";


      try {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, mssg, null, null);

      } catch (Exception e) {

        e.printStackTrace();
      }

    }



    ////////////////////////////
    //get the string from strings.xml and format
    String messageString = context.getString(R.string.messageRecieved, (Object[]) args);

    //create intent to start activity
    Intent intent = new Intent();
    intent.setClassName(context, "org.eclipse.paho.android.service.sample.ConnectionDetails");
    intent.putExtra("handle", clientHandle);

    //format string args
    Object[] notifyArgs = new String[3];
    notifyArgs[0] = c.getId();
    notifyArgs[1] = new String(message.getPayload());
    notifyArgs[2] = topic;

    //notify the user
    Notify.notifcation(context, context.getString(R.string.notification, notifyArgs), intent, R.string.notifyTitle);

    //update client history
    c.addAction(messageString);

  }






  /**
   * @see org.eclipse.paho.client.mqttv3.MqttCallback#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)
   */
  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    // Do nothing
  }


  public void onLocationChanged(Location location) {

    Toast.makeText(getBaseContext(),"Current Longitude:" + location.getLongitude() , Toast.LENGTH_SHORT).show();
    Toast.makeText(getBaseContext(),"Current Latitude:" + location.getLatitude(), Toast.LENGTH_SHORT).show();
    Current_latitude =" Current Latitude:" + location.getLatitude();
    Current_longitude=" Current Longitude:" + location.getLongitude();

    Log.e("location", "Current Latitude:" + location.getLatitude() );
    Log.e("location", "Current Longitude:" + location.getLongitude()  );



  }

  @Override
  public void onStatusChanged(String s, int i, Bundle bundle) {

  }

  @Override
  public void onProviderEnabled(String s) {

  }

  @Override
  public void onProviderDisabled(String s) {

  }





}
