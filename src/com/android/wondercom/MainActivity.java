package com.android.wondercom;

import com.android.wondercom.InitThreads.ClientInit;
import com.android.wondercom.InitThreads.ServerInit;
import com.android.wondercom.Receivers.WifiDirectBroadcastReceiver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/*
 * This activity is the launcher activity. 
 * Once the connection established, the ChatActivity is launched.
 */
public class MainActivity extends Activity{
	public static final String TAG = "MainActivity";	
	private WifiP2pManager mManager;
	private Channel mChannel;
	private WifiDirectBroadcastReceiver mReceiver;
	private IntentFilter mIntentFilter;
	private Button goToChat;
	private Button goToSettings;
	private TextView setChatNameLabel;
	private EditText setChatName;
	public static String chatName;

	//Getters and Setters
    public WifiP2pManager getmManager() { return mManager; }
	public Channel getmChannel() { return mChannel; }
	public WifiDirectBroadcastReceiver getmReceiver() { return mReceiver; }
	public IntentFilter getmIntentFilter() { return mIntentFilter; }
	public Button getGoToChat(){ return goToChat; }
	public TextView getSetChatNameLabel() { return setChatNameLabel; }
	public Button getGoToSettings() { return goToSettings; }
	public EditText getSetChatName() { return setChatName; }
	
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); 
        
        //Init the Channel, Intent filter and Broadcast receiver
        init();
        
        //Button Go to Settings
        goToSettings = (Button) findViewById(R.id.goToSettings);
        goToSettings();
        
        
        //Button Go to Chat
        goToChat = (Button) findViewById(R.id.goToChat);
        goToChat.setVisibility(View.GONE);
        goToChat();
        
        //Set the chat name
        setChatName = (EditText) findViewById(R.id.setChatName);
        setChatNameLabel = (TextView) findViewById(R.id.setChatNameLabel);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
        
		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
					
			@Override
			public void onSuccess() {
				Log.v(TAG, "Discovery process succeeded");
			}
			
			@Override
			public void onFailure(int reason) {
				Log.v(TAG, "Discovery process failed");
			}
		});
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int idItem = item.getItemId();
        if (idItem == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }	
    
    public void init(){
    	mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = WifiDirectBroadcastReceiver.createInstance();
        mReceiver.setmManager(mManager);
        mReceiver.setmChannel(mChannel);
        mReceiver.setmActivity(this);
        
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }
    
    public void goToChat(){
    	goToChat.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(!setChatName.getText().toString().equals("")){
					//Set the chat name
					chatName = setChatName.getText().toString();
					
					//Start the init process
					if(mReceiver.isGroupeOwner() ==  WifiDirectBroadcastReceiver.IS_OWNER){
						Toast.makeText(MainActivity.this, "I'm the group owner  " + mReceiver.getOwnerAddr().getHostAddress(), Toast.LENGTH_SHORT).show();
						ServerInit server = new ServerInit();
						server.start();
					}
					else if(mReceiver.isGroupeOwner() ==  WifiDirectBroadcastReceiver.IS_CLIENT){
						Toast.makeText(MainActivity.this, "I'm the client", Toast.LENGTH_SHORT).show();
						ClientInit client = new ClientInit(mReceiver.getOwnerAddr());
						client.start();
					}
					else if(mReceiver.isGroupeOwner() ==  WifiDirectBroadcastReceiver.GROUP_NOT_FORMED){
						Toast.makeText(MainActivity.this, "Error: The group is not formed", Toast.LENGTH_SHORT).show();
					}
					
					//Open the ChatActivity
					Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
					startActivity(intent);
				}
				else{
					Toast.makeText(MainActivity.this, "Please enter a chat name", Toast.LENGTH_SHORT).show();
				}					
			}
		});    	
    }
    
    public void goToSettings(){    	
    	goToSettings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				//Open Wifi settings
		        startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), 0);
			}
		});    	
    }
}