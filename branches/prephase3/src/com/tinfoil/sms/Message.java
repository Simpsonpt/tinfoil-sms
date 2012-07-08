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

import java.util.Calendar;

/**
 * A class used to store information from the numbers table
 *
 */
public class Message {
	
	private String message;
	private long date;
	
	/**
	 * 
	 * @param number
	 * @param type
	 * @param lastMessage
	 * @param date
	 */
	public Message (String message, long date)
	{
		this.setMessage(message);
		this.setDate(date);
	}
	
	/**
	 * @param number
	 * @param lastMessage
	 */
	public Message (String message, boolean currentTime)
	{
		this.setMessage(message);
		if (currentTime)
		{
			this.setDate();
		}
		else
		{
			this.setDate(0);
		}
	}

	/**
	 * @return the lastMessage
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param lastMessage the lastMessage to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the date
	 */
	public long getDate() {
		return date;
	}

	/**
	 * @param date the date to set
	 */
	public void setDate(long date) {
		this.date = date;
	}
	
	/**
	 * set the date to the current time
	 */
	public void setDate() {
		Calendar calendar = Calendar.getInstance();
		this.date = calendar.getTimeInMillis();
	}
	
	public static String millisToDate(long currentTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        return calendar.getTime().toString();
    }
}
