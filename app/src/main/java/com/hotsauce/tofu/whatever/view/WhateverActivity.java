package com.hotsauce.tofu.whatever.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.hotsauce.tofu.whatever.R;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artists;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class WhateverActivity extends AppCompatActivity implements PlayerNotificationCallback, ConnectionStateCallback {
	private static String TAG = WhateverActivity.class.getSimpleName();

	/**
	 * Client ID for spotify API
	 */
	private static final String CLIENT_ID = "6f90794547034f90922e1b162dbc86f9";

	/**
	 * Redirect URI for spotify API
	 */
	private static final String REDIRECT_URI = "hotsauce://login-callback";

	/**
	 * Track URI
	 */
	private static final String TRACK_URI = "spotify:track:7C7gszJBmB2ODqFyBsJkIb";

	private static final String OG_ARTIST_URI = "3AmgGrYHXqgbmZ2yKoIVzO";
	private static final String OG_ALBUM_URI = "1LI819VWBwPDnLtIGP9UMC";

	// Request code that will be used to verify if the result comes from correct activity
	// Can be any integer
	private static final int REQUEST_CODE = 1337;

	private Player mPlayer;

	@Bind(R.id.control_panel)
	View mContentView;

	@Bind(R.id.play)
	TextView mPlay;

	@Bind(R.id.pause)
	TextView mPause;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.whatever_activity);
		ButterKnife.bind(this);
		loginSpotify();
		fullscreenAllOfIt();
	}

	@Override
	protected void onDestroy() {
		// must destroy player or resources will leak
		Spotify.destroyPlayer(this);
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		// Check if result comes from the correct activity
		if (requestCode == REQUEST_CODE) {
			AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
			if (response.getType() == AuthenticationResponse.Type.TOKEN) {
				Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
				Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
					@Override
					public void onInitialized(Player player) {
						mPlayer = player;
						mPlayer.addConnectionStateCallback(WhateverActivity.this);
						mPlayer.addPlayerNotificationCallback(WhateverActivity.this);
						bindListeners();
//						mPlayer.play(TRACK_URI);
					}

					@Override
					public void onError(Throwable throwable) {
						Log.e(TAG, "Could not initialize player: " + throwable.getMessage());
					}
				});

				spotifyWebApiCalls(response);
			}
		}
	}

	private void spotifyWebApiCalls(AuthenticationResponse response) {
		final SpotifyApi api = new SpotifyApi();
		api.setAccessToken(response.getAccessToken());
		final SpotifyService spotifyService = api.getService();

		getAlbum(spotifyService, OG_ALBUM_URI);
		getRelatedArtists(spotifyService, OG_ARTIST_URI);
	}

	private void getRelatedArtists(SpotifyService spotifyService, String artistUri) {
		spotifyService.getRelatedArtists(artistUri, new Callback<Artists>() {
			@Override
			public void success(Artists artists, Response response) {
				Log.d(TAG, "RELATED ARTISTS: " + artists.toString());
			}

			@Override
			public void failure(RetrofitError error) {
				Log.d(TAG, "RELATED ARTIST FAIL");
			}
		});
	}

	private void getAlbum(SpotifyService spotifyService, String albumUri) {
		spotifyService.getAlbum(albumUri, new Callback<Album>() {
			@Override
			public void success(Album album, retrofit.client.Response response) {
				Log.d(TAG, "Album success: " + album.name);
			}

			@Override
			public void failure(RetrofitError error) {
				Log.d(TAG, "Album failure: " + error.toString());
			}
		});
	}

	private void fullscreenAllOfIt() {
		// Note that some of these constants are new as of API 16 (Jelly Bean)
		// and API 19 (KitKat). It is safe to use them, as they are inlined
		// at compile-time and do nothing on earlier devices.
		mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	}

	private void loginSpotify() {
		final AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
				AuthenticationResponse.Type.TOKEN,
				REDIRECT_URI);
		builder.setScopes(new String[]{"user-read-private", "streaming"});
		AuthenticationRequest request = builder.build();

		AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
	}

	private void bindListeners() {
		mPlay.setOnClickListener(v -> mPlayer.play(TRACK_URI));
		mPlayer.addPlayerNotificationCallback(new PlayerNotificationCallback() {
			@Override
			public void onPlaybackEvent(EventType eventType, PlayerState playerState) {

			}

			@Override
			public void onPlaybackError(ErrorType errorType, String s) {

			}
		});

		mPause.setOnClickListener(v -> mPlayer.pause());
	}

	@Override
	public void onLoggedIn() {

	}

	@Override
	public void onLoggedOut() {

	}

	@Override
	public void onLoginFailed(Throwable throwable) {

	}

	@Override
	public void onTemporaryError() {

	}

	@Override
	public void onConnectionMessage(String s) {

	}

	@Override
	public void onPlaybackEvent(EventType eventType, PlayerState playerState) {

	}

	@Override
	public void onPlaybackError(ErrorType errorType, String s) {

	}
}
