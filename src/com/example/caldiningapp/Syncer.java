package com.example.caldiningapp;
import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


import android.R;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;


public class Syncer extends AsyncTask<Void, Void, Document>{

	public static ArrayList<String> cloneList(ArrayList<String> list) {
		ArrayList<String> clone = new ArrayList<String>(list.size());
		for(String item: list) clone.add(new String(item));
		return clone;
	}

	@Override
	protected Document doInBackground(Void... params) {
		Document doc = null;
		try {
			doc = Jsoup.connect("http://services.housing.berkeley.edu/FoodPro/dining/static/todaysentrees.asp").get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return doc;
	} 


	@Override
	protected void onPostExecute(Document result) {
		Elements scraped = result.select("table table td");
		Object[] tables = scraped.toArray();
		if (result == null || tables.length == 0) {
			ArrayList<String> errorArr = new ArrayList<String>();
			errorArr.add(new String("Cannot fetch menu; no internet connection."));
			for (int i = 0; i <= 11; i++) {
				MainActivity.menu[i] = errorArr;
			}
		}
		else {
			ArrayList<String>[] menu = new ArrayList[12];
			//if you had a ui element, you could display the title
			ArrayList<String> curr = new ArrayList<String>(); // The current menu for a place at a time (e.g. Crossroads Lunch)
			int MenuIn = 0;
			for (int i = 15; i <= 26; i++) {
				String blocks = (String) tables[i].toString().split("<hr />")[1]; // All items for a specific meal (e.g. all dinner items)
				if (!blocks.contains("Closed")) { // Only do the following if it's not closed
					String [] item = blocks.split("<br />"); // Get each item
					for (int j = 0; j < item.length - 3; j++) {
						String name = (item[j].split(">")[2].split("</")[0]).replace("&amp;", "&");
						String type = item[j].substring(9).split("#")[1].split(">")[0];
						type = type.substring(0,type.length()-1);
						if (type.equals("800040")){
							type = "**";
						} else if (type.equals("008000")){
							type = "*";
						} else if (type.equals("000000") || type.equals("0F0F0F")){
							type = "";
						}
						curr.add(name + type);
					}
				}
				ArrayList<String> insert = cloneList(curr);
				if (insert.size() == 0){
					insert.add("Closed");
				}
				menu[MenuIn] = insert; // Put the current list of items in the menu for a specific place and time (e.g. all Crossroads dinner items)
				curr.clear();
				MenuIn++;
			}
			MainActivity.menu = menu;
		}
	}
}
