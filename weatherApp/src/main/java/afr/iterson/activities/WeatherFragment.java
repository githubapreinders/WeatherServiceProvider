package afr.iterson.activities;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import retrofit.RestAdapter;
import afr.iterson.R;
import afr.iterson.provider.WeatherContract.WeatherDataEntry;
import afr.iterson.retrofitWeather.GeoNamesServiceProxy;
import afr.iterson.retrofitWeather.TimeModifier;
import afr.iterson.retrofitWeather.WeatherData;
import afr.iterson.retrofitWeather.WeatherForecast;
import afr.iterson.retrofitWeather.WeatherWebServiceProxy;
import afr.iterson.utils.DataMediator;
import afr.iterson.utils.Utils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherFragment extends android.support.v4.app.Fragment
{

	BroadcastReceiver _broadcastReceiver;

	BroadcastReceiver _updaterReceiver;

	public static String ACTION_UPDATE_FRAGMENT = "afr.iterson.update.fragment";

	@SuppressLint("SimpleDateFormat")
	private final SimpleDateFormat _sdfWatchTime = new SimpleDateFormat("HH:mm");

	interface UpdaterCache
	{
		WeatherData getFromCache(long cityid);
	}

	private UpdaterCache mCache;

	public static final String TAG = WeatherFragment.class.getSimpleName();

	private TextView mTextView1City, mTextView2Coun, mTextView3Temp, mTextView4Wind, mTextView6Hum, mTextView7Sunr,
			mTextView8Suns, mTextView9Date, mTextView10Time;

	private LinearLayout mLinearLayout1;

	private ImageView mImageView5Angle;

	private String identifier;

	private Bundle state;

	private WeakReference<Activity> mActivity;

	private boolean metric;

	private int mTimeCheater;

	public WeatherFragment()
	{

	}

	public static WeatherFragment newInstance(WeatherData weatherdata)
	{

		Log.d(TAG, "newInstance of Fragment");
		WeatherFragment fragment = new WeatherFragment();
		fragment.setArguments(makeBundle(weatherdata));
		fragment.setIdentifier(weatherdata.getmName() + "#" + weatherdata.getmSys().getCountry());
		return fragment;
	}

	public static Bundle makeBundle(WeatherData weatherdata)
	{
		Bundle bundle1 = new Bundle();
		bundle1.putString(Utils.KEY_CITY, weatherdata.getmName());
		bundle1.putLong(Utils.KEY_CITYID, weatherdata.getmId());
		bundle1.putString(Utils.KEY_COUNTRY, weatherdata.getSys().getCountry());
		bundle1.putDouble(Utils.KEY_TEMPERATURE, weatherdata.getMain().getTemp());
		bundle1.putDouble(Utils.KEY_WINDSPEED, weatherdata.getWind().getSpeed());
		bundle1.putDouble(Utils.KEY_WINDANGLE, weatherdata.getWind().getDeg());
		bundle1.putLong(Utils.KEY_HUM, weatherdata.getMain().getHumidity());
		bundle1.putString(Utils.KEY_SUNRISESTRING, Utils.convertStringToTime(weatherdata.getSunriseString()));
		bundle1.putString(Utils.KEY_SUNSETSTRING, Utils.convertStringToTime(weatherdata.getSunsetString()));
		bundle1.putString(Utils.KEY_TIMECHEATER, weatherdata.getDateString());
		bundle1.putString(Utils.KEY_LOCALTIME, Utils.convertDate(weatherdata.getDateString()));
		bundle1.putDouble(Utils.KEY_GMTOFFSET, weatherdata.getGmtOffset());
		bundle1.putString(Utils.KEY_ICON, weatherdata.getMainicon());
		bundle1.putStringArray(Utils.Key_ICONS_FORECAST, weatherdata.getIcons());
		return bundle1;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.d(TAG, "Fragment onCreate()");

		super.onCreate(savedInstanceState);
		// get the preference for metric or Amerindian display units.
		metric = ((WeatherApp) getActivity().getApplication()).isMetric();
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.d(TAG, "Fragment onCreateView()");

		View view = inflater.inflate(R.layout.weather_activity, container, false);

		Bundle bundle;
		if (savedInstanceState == null)
		{
			Log.d(TAG, "Bundle from arguments");

			bundle = getArguments();
		} else
		{
			Log.d(TAG, "Bundle from savedInstanceState");
			bundle = savedInstanceState;
		}

		mTimeCheater = Integer.parseInt((bundle.getString(Utils.KEY_TIMECHEATER).substring(11, 13)));

		mTextView1City = (TextView) view.findViewById(R.id.textView1);

		mTextView2Coun = (TextView) view.findViewById(R.id.textView2);

		mTextView3Temp = (TextView) view.findViewById(R.id.textView3);
		mTextView3Temp.setOnClickListener(new TemperatureTextViewClickListener());

		mTextView4Wind = (TextView) view.findViewById(R.id.textView4);

		mImageView5Angle = (ImageView) view.findViewById(R.id.imageView5);

		mTextView6Hum = (TextView) view.findViewById(R.id.textView6);

		mTextView7Sunr = (TextView) view.findViewById(R.id.textView7);

		mTextView8Suns = (TextView) view.findViewById(R.id.textView8);

		mTextView9Date = (TextView) view.findViewById(R.id.textView9);

		mTextView10Time = (TextView) view.findViewById(R.id.textView10);
		mTextView10Time.setText(_sdfWatchTime.format(getCalendarForNow().getTime()));

		mLinearLayout1 = (LinearLayout) view.findViewById(R.id.linearLayout1);
		mLinearLayout1.setClickable(true);
		mLinearLayout1.setOnClickListener(new LinearLayoutClickListener());

		fillInViews(bundle);

		return view;
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		mActivity = new WeakReference<>(activity);
		mCache = (UpdaterCache) activity;
	}

	@SuppressWarnings("deprecation")
	public void fillInViews(Bundle bundle)
	{
		Log.d(TAG, "Fragment fillInViews()");

		state = bundle;

		mTextView1City.setText(bundle.getString(Utils.KEY_CITY));
		mTextView2Coun.setText(bundle.getString(Utils.KEY_COUNTRY));
		mTextView3Temp.setText(Utils.formatTemperature(getActivity().getBaseContext(),
				bundle.getDouble(Utils.KEY_TEMPERATURE), metric));
		mTextView4Wind.setText(Utils.convertToBeaufort(bundle.getDouble(Utils.KEY_WINDSPEED)));
		mImageView5Angle.setImageDrawable(mActivity.get().getResources()
				.getDrawable(Utils.convertToWinddirectionDrawable((int) bundle.getDouble(Utils.KEY_WINDANGLE))));
		mTextView6Hum.setText("Humidity " + String.valueOf(bundle.getLong(Utils.KEY_HUM)) + "\u0025");
		mTextView7Sunr.setText(bundle.getString(Utils.KEY_SUNRISESTRING));
		mTextView8Suns.setText(bundle.getString(Utils.KEY_SUNSETSTRING));
		mTextView9Date.setText(bundle.getString(Utils.KEY_LOCALTIME));
		Log.d(TAG, "Icon: " + bundle.getString(Utils.KEY_ICON));
		mLinearLayout1.setBackgroundDrawable(mActivity.get().getResources()
				.getDrawable(Utils.getWeatherDrawableId(bundle.getString(Utils.KEY_ICON))));
		mLinearLayout1.setOnClickListener(new LinearLayoutClickListener());
	}

	class LinearLayoutClickListener implements View.OnClickListener
	{

		@Override
		public void onClick(View v)
		{
		}

	}

	/**
	 * Converts temperature to Celsius or Vice Versa , adds it to the display
	 * and saves to shared preferences.
	 * 
	 * @author ap
	 */
	class TemperatureTextViewClickListener implements View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			Log.d(TAG, "Temperature ClickHandler(), metric " + ((WeatherApp) getActivity().getApplication()).isMetric());
			metric = !metric;
			((WeatherApp) getActivity().getApplication()).setMetric(metric);
			mTextView3Temp.setText(Utils.formatTemperature(getActivity().getBaseContext(),
					(int) state.getDouble(Utils.KEY_TEMPERATURE), metric));
		}
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public Bundle getState()
	{
		return state;
	}

	@Override
	public void onDestroyView()
	{
		Log.d(TAG, "onDestroyView, unregistering observer...");
		super.onDestroyView();
		mActivity = null;
	}

	public String toString()
	{
		return getIdentifier();
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		outState.putString(Utils.KEY_CITY, state.getString(Utils.KEY_CITY));
		outState.putLong(Utils.KEY_CITYID, state.getLong(Utils.KEY_CITYID));
		outState.putString(Utils.KEY_COUNTRY, state.getString(Utils.KEY_COUNTRY));
		outState.putDouble(Utils.KEY_TEMPERATURE, state.getDouble(Utils.KEY_TEMPERATURE));
		outState.putDouble(Utils.KEY_WINDSPEED, state.getDouble(Utils.KEY_WINDSPEED));
		outState.putDouble(Utils.KEY_WINDANGLE, state.getDouble(Utils.KEY_WINDANGLE));
		outState.putLong(Utils.KEY_HUM, state.getLong(Utils.KEY_HUM));
		outState.putString(Utils.KEY_SUNRISESTRING, state.getString(Utils.KEY_SUNRISESTRING));
		outState.putString(Utils.KEY_SUNSETSTRING, state.getString(Utils.KEY_SUNSETSTRING));
		outState.putString(Utils.KEY_LOCALTIME, state.getString(Utils.KEY_LOCALTIME));
		outState.putString(Utils.KEY_ICON, state.getString(Utils.KEY_ICON));
		outState.putDouble(Utils.KEY_GMTOFFSET, state.getDouble(Utils.KEY_GMTOFFSET));
		outState.putString(Utils.KEY_TIMECHEATER, state.getString(Utils.KEY_TIMECHEATER));
		super.onSaveInstanceState(outState);
	}

	class UpdaterTask extends AsyncTask<Long, Void, WeatherData>
	{

		@Override
		protected WeatherData doInBackground(Long... params)
		{
			Log.d(TAG, "Updatertask in background thread...");
			return mCache.getFromCache(params[0]);
		}

		@Override
		protected void onPostExecute(final WeatherData result)
		{
			Log.d(TAG, "onPostExecute()...");
			if (result != null)
			{
				mActivity.get().runOnUiThread(new Runnable()
				{

					@Override
					public void run()
					{
						Log.d(TAG, "runnable updating views...");
						fillInViews(makeBundle(result));
					}
				});
			}
		}
	}

	/**
	 * Called from onStart(); When the fragment returns from being stopped it
	 * needs fresh values from the web to update the display immediately;
	 * 
	 * @author ap
	 *
	 */
	class StartupTask extends AsyncTask<Long, Void, WeatherData>
	{

		@Override
		protected WeatherData doInBackground(Long... params)
		{
			WeatherData weatherData;
			Log.d(TAG, "StartupTask in background thread...");
			WeatherWebServiceProxy mWeatherWebServiceProxy = new RestAdapter.Builder()
					.setEndpoint(Utils.sWeather_Service_URL_Retro).build().create(WeatherWebServiceProxy.class);

			GeoNamesServiceProxy mGeoNamesServiceProxy = new RestAdapter.Builder().setEndpoint(Utils.Geo_Names_URL)
					.build().create(GeoNamesServiceProxy.class);

			weatherData = mWeatherWebServiceProxy.getWeatherDataById(params[0]);

			// Check to make sure the call to the server succeeded by
			// testing the "name" member to make sure it was
			// initialized.
			if (weatherData.getName() == null || weatherData.getName().equals(""))
				return null;

			WeatherForecast forecast = mWeatherWebServiceProxy.getWeatherForecast(weatherData.getName(),
					Utils.DAYS_AHEAD);
			// Add to cache.

			TimeModifier modifier = mGeoNamesServiceProxy.getModifiers(weatherData.getCoord().getLon(), weatherData
					.getCoord().getLat(), Utils.USERNAME);

			weatherData = DataMediator.modifyWeatherData(forecast, modifier, weatherData);

			return weatherData;
		}

		/**
		 * Postponing updating the view cause a task on first startup might
		 * still be running
		 */
		@Override
		protected void onPostExecute(final WeatherData result)
		{
			Log.d(TAG, "onPostExecute()...");
			if (result != null)
			{
				final Handler handler = new Handler();
				handler.postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						String value = result.getDateString();
						state.putString(Utils.KEY_TIMECHEATER, value);
						mTimeCheater = Integer.parseInt(value.substring(11,13));
						mTextView10Time.setText(_sdfWatchTime.format(getCalendarForNow().getTime()));
						fillInViews(makeBundle(result));
						Log.v(TAG, "ViewsFilled in...");
					}
				}, 1000);
			}
		}
	}

	@Override
	public void onStart()
	{
		Log.d(TAG, "onStart()... ");
		super.onStart();
		Long l = state.getLong(Utils.KEY_CITYID);

		// Gets the data from the web;
		new StartupTask().execute(l);

		// Defines a receiver that will trigger every minute to update the time
		// field
		_broadcastReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0)
				{
					mTextView10Time.setText(_sdfWatchTime.format(getCalendarForNow().getTime()));
				}
			}
		};
		mActivity.get().registerReceiver(_broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

		// Defines a receiver that will trigger when the WeatherService has
		// finished its job.
		// All the values in the fragment will be updated.
		_updaterReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				Log.d(TAG, "updater broadcast received... ");
				new UpdaterTask().execute(state.getLong(Utils.KEY_CITYID));
			}
		};
		mActivity.get().registerReceiver(_updaterReceiver, new IntentFilter(WeatherFragment.ACTION_UPDATE_FRAGMENT));
	}

	/**
	 * Gives a Calendar object that is adapted according to the GeoNames given
	 * datestring; The string says "2015-07-28 21:14" and the part 21 is
	 * filtered out as the mTimeCheater; we append the minutes from the calendar
	 * object every minut to get a changing clock that counts for the given
	 * locale. This hack avoids the tedious hazard of getting corrections for
	 * daylight savings time and the like.
	 * 
	 * @return
	 */
	public Calendar getCalendarForNow()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, mTimeCheater);
		if (calendar.get(Calendar.MINUTE) == 59)
		{
			if (mTimeCheater == 23)
			{
				Log.d(TAG, "New Day...changing date... ");
				mTimeCheater = 0;
				new UpdaterTask().execute(state.getLong(Utils.KEY_CITYID));
			} else
			{
				mTimeCheater++;
			}
		}
		return calendar;
	}

	/**
	 * Unregister the BroadcastReceivers
	 */
	@Override
	public void onStop()
	{
		super.onStop();
		if (_broadcastReceiver != null)
		{
			mActivity.get().unregisterReceiver(_broadcastReceiver);
		}
		if (_updaterReceiver != null)
		{
			mActivity.get().unregisterReceiver(_updaterReceiver);
		}

	}

	@Override
	public void onResume()
	{
		super.onResume();

	}

}
