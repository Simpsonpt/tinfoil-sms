/** 
 * Copyright (C) 2011 Tinfoilhat
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tinfoil.sms;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.ListView;

/**
 * ManageContactActivity is an activity that allows the user to exchange keys, 
 * edit and delete contacts. A list of contacts will be shown with an check box,
 * if check then the user is either exchanging or have exchanged keys with the
 * contact. To edit a contact's information hold down for a long press, which 
 * will start AddContact activity with addContact == false and editTc != null. A
 * contact can be added by click 'Add Contact' in the menu this will start the 
 * AddContact activity with addContact == true and editTc == null. Contacts can
 * be deleted from tinfoil-sms's database by clicking 'Delete Contact' in the
 * menu which will start RemoveContactActivity. 
 */
public class ManageContactsActivity extends Activity implements Runnable {

	private ExpandableListView extendableList;
	private ListView listView;
	private Button exchangeKeys;
	private ArrayList<TrustedContact> tc;
	private ProgressDialog loadingDialog;
	private ArrayAdapter<String> arrayAp;
	private boolean[] trusted;
	
	private ArrayList<ContactParent> contacts;
	private ArrayList<ContactChild> contactNumbers;
	private static ManageContactAdapter adapter;

	private static ExchangeKey keyThread = new ExchangeKey();

    /** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.contact);
        extendableList = (ExpandableListView)findViewById(R.id.contacts_list);
        listView = (ListView)findViewById(R.id.empty_list);
        exchangeKeys = (Button)findViewById(R.id.exchange_keys);
     
        extendableList.setOnItemLongClickListener(new OnItemLongClickListener(){

			public boolean onItemLongClick(AdapterView<?> parent, View view,
        			int position, long id) {
				
				AddContact.addContact = false;
				AddContact.editTc = tc.get(position);
				ManageContactsActivity.this.startActivity(new Intent
						(ManageContactsActivity.this, AddContact.class));

				//This stops other on click effects from happening after this one.
				return true; 
			}
        });
        
        listView.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> parent, View view,
        		int position, long id) {
				
				//Go to add contact
    			AddContact.addContact = true;
				AddContact.editTc = null;
    			startActivity(new Intent(getBaseContext(), AddContact.class));
			}        	
        });
        
        extendableList.setOnChildClickListener(new OnChildClickListener(){

			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				
				CheckedTextView checked_text = (CheckedTextView)v.findViewById(R.id.trust_name);
				
				adapter.getContacts().get(groupPosition).getNumber(childPosition).toggle();
				
				checked_text.setChecked(adapter.getContacts().get(groupPosition).getNumber(childPosition).isSelected());

				return true;
			}});
        
        exchangeKeys.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				/*
				 * Launch Exchange Keys thread.
				 */				
				ExchangeKey.keyDialog = ProgressDialog.show(ManageContactsActivity.this, "Exchanging Keys", 
		                "Exchanging. Please wait...", true, false);
				
				keyThread.startThread(ManageContactsActivity.this, adapter.getContacts());
				
				ExchangeKey.keyDialog.setOnDismissListener(new OnDismissListener(){

					public void onDismiss(DialogInterface dialog) {
						startThread();
					}					
				});
			}});
	}
	
	/**
	 * Updates the list of contacts
	 */
	private void update()
	{
		if(tc != null)
		{
			extendableList.setAdapter(adapter);
			listView.setVisibility(ListView.INVISIBLE);
			extendableList.setVisibility(ListView.VISIBLE);
		}
		else
		{
			listView.setAdapter(arrayAp);
			extendableList.setVisibility(ListView.INVISIBLE);
			listView.setVisibility(ListView.VISIBLE);
		}
		//listView.setItemsCanFocus(false);
		
		if (tc != null)
        {
			extendableList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        }
	}
	
	/*
	 * Added the onResume to update the list of contacts
	 */
	protected void onResume()
	{
		super.onResume();
		startThread();		
	}
	
	private void startThread()
	{
		//TODO Override dialog to make so if BACK is pressed it exits the activity if it hasn't finished loading
        loadingDialog = ProgressDialog.show(this, "Loading Contacts", 
                "Loading. Please wait...", true, false);
		Thread thread = new Thread(this);
	    thread.start();
		super.onResume();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.manage_contacts_menu, menu);
		return true;		
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add:
		{			
			AddContact.addContact = true;
			AddContact.editTc = null;
			startActivity(new Intent(this, AddContact.class));
			
			return true;
		}
		case R.id.all:
			if (tc!=null)
			{
				//TODO re-implement
				/*ArrayList<ContactParent> contact = adapter.getContacts();
				for(int i = 0; i < contact.size(); i++)
				{
					adapter.getContacts().get(groupPosition).getNumber(childPosition).toggle();
					
					checked_text.setChecked(adapter.getContacts().get(groupPosition).getNumber(childPosition).isSelected());
				}*/
			}
			return true;
		case R.id.remove:
			if (tc!=null)
			{
				//TODO re-implement
				/*for (int i = 0; i < tc.size();i++)
				{
					listView.setItemChecked(i, false);
					change(i, false);
				}*/
			}
			return true;
		case R.id.delete:
		{
			if (tc!=null)
			{
				startActivity(new Intent(getApplicationContext(), RemoveContactsActivity.class));
			}
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	public void run() {
		tc = MessageService.dba.getAllRows();
		
		if (tc != null)
		{			
			contacts = new ArrayList<ContactParent>();
			int size = 0;
			
	        for (int i = 0; i < tc.size(); i++)
	        {
	        	size = tc.get(i).getNumber().size();
	       
	        	contactNumbers = new ArrayList<ContactChild>();
	        	
	        	trusted = MessageService.dba.isNumberTrusted(tc.get(i).getNumber());
	        	
	        	for(int j = 0; j < size; j++)
	        	{	        			
		       		//TODO change to use primary key from trusted contact table
	        		contactNumbers.add(new ContactChild(tc.get(i).getNumber(j), 
	        				trusted[j],false));
	        	}
	        	contacts.add(new ContactParent(tc.get(i).getName(), contactNumbers));
	        }
	        
	        adapter = new ManageContactAdapter(this, contacts);
	    }
		else
		{			
			//TODO fix
        	
        	arrayAp = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
        			new String[]{"Add a Contact"});
		}		
		
		handler.sendEmptyMessage(0);
	}
	
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg)
        {
        	update();
        	loadingDialog.dismiss();
        }
	};

}