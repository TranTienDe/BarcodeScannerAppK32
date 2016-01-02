package com.grobestvietnam.mediaplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;


public class Player {
	
	private Context _context;
	private String TAB = "ScaleNote";
	
	public Player(Context context)	{
		_context = context;		
	}
	
	public MediaPlayer getPlayerInstance(int sound) {
		Log.d(TAB,"Sound ID: " + sound);
		return MediaPlayer.create(_context, sound);
	}
	
}
