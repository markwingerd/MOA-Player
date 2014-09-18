package us.reimus.moaplayer;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


public class DirectoryAdapter extends BaseAdapter {
	private ArrayList<String> directories;
	private LayoutInflater directoryInf;
	
	public DirectoryAdapter(Context c, ArrayList<String> theDirectories) {
		directories = theDirectories;
		directoryInf = LayoutInflater.from(c);
	}

	@Override
	public int getCount() {
		return directories.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Map to directory layout.
		LinearLayout directoryLay;
		if (convertView == null) {
			directoryLay = (LinearLayout)directoryInf.inflate(R.layout.directory, parent, false);
		}
		else {
			directoryLay = (LinearLayout)directoryInf.inflate(R.layout.directory, parent);
		}
		// Get directory name.
		TextView directoryView = (TextView)directoryLay.findViewById(R.id.directory_name);
		// Get directory using position.
		//String currDirectory = directories.get(position); // NO LONGER NEEDED?
		// Get directory strings.
		directoryView.setText(directories.get(position));
		// Set position as tag.
		directoryLay.setTag(position);
		
		///////
		///////
	    return directoryLay;
	}

}
