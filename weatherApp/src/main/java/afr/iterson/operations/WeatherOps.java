package afr.iterson.operations;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import retrofit.RestAdapter;
import afr.iterson.operations.MyRecyclerViewAdapter.ViewHolder;
import afr.iterson.provider.cache.UpdateReceiver;
import afr.iterson.provider.cache.WeatherTimeoutCache;
import afr.iterson.retrofitWeather.GeoNamesServiceProxy;
import afr.iterson.retrofitWeather.TimeModifier;
import afr.iterson.retrofitWeather.WeatherData;
import afr.iterson.retrofitWeather.WeatherForecast;
import afr.iterson.retrofitWeather.WeatherWebServiceProxy;
import afr.iterson.service.WeatherService;
import afr.iterson.utils.ConfigurableOps;
import afr.iterson.utils.ContextView;
import afr.iterson.utils.DataMediator;
import afr.iterson.utils.GenericAsyncTask;
import afr.iterson.utils.GenericAsyncTaskOps;
import afr.iterson.utils.Utils;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ListView;

/**
 * This class implements all the weather-related operations defined in the
 * WeatherOps interface.
 */
public class WeatherOps implements ConfigurableOps<WeatherOps.View>, GenericAsyncTaskOps<String, Void, WeatherData>
{
	protected static final String RFM_KEY = "doInBackgroundResult";

	/**
	 * This interface defines the minimum interface needed by the VideoOps class
	 * in the "Presenter" layer to interact with the VideoListActivity in the
	 * "View" layer.
	 */
	public interface View extends ContextView
	{
		/**
		 * Finishes the Activity the WeatherOps is associated with.
		 */
		void finish();

		/**
		 * Sets the Adapter that contains List of Videos.
		 */
		void displayResults(WeatherData mCurrentWeatherData);
	}

	/**
	 * Debugging tag used by the Android logger.
	 */
	protected final String TAG = getClass().getSimpleName();

	/**
	 * Used to enable garbage collection.
	 */
	protected WeakReference<WeatherOps.View> mWeatherView;

	/**
	 * Cache for the WeatherData.
	 */
	private WeatherTimeoutCache mCache;

	//private MyCbArrayAdapter mAdapter;

	
	private MyRecyclerViewAdapter adapter;
	
	
	private ArrayList<City> mCitylist;
	
	
	
	/**
	 * Retrofit proxy that sends requests to the Weather Service web service and
	 * converts the Json response to an instance of AcronymData POJO class.
	 */
	private WeatherWebServiceProxy mWeatherWebServiceProxy;

	private GeoNamesServiceProxy mGeoNamesServiceProxy;

	/**
	 * WeatherData object that is being displayed, which is used to re-populate
	 * the UI after a runtime configuration change.
	 */
	private WeatherData mCurrentWeatherData;

	/**
	 * The GenericAsyncTask used to get the current weather from the Weather
	 * Service web service.
	 */
	private GenericAsyncTask<String, Void, WeatherData, WeatherOps> mAsyncTask;

	/**
	 * Default constructor that's needed by the GenericActivity framework.
	 */
	public WeatherOps()
	{
	}

	
	
	
	/**
	 * Called by the WeatherOps constructor and after a runtime configuration
	 * change occurs to finish the initialization steps.
	 */
	public void onConfiguration(WeatherOps.View view, boolean firstTimeIn)
	{
		// Reset the mActivity WeakReference.
		mWeatherView = new WeakReference<>((WeatherOps.View) view);

		if (firstTimeIn)
		{
			// Initialize the TimeoutCache.
			mCache = new WeatherTimeoutCache(mWeatherView.get().getApplicationContext());
			
			// Build the RetroFit RestAdapter, which is used to create
			// the RetroFit service instance, and then use it to build
			// the RetrofitWeatherServiceProxy.
			mWeatherWebServiceProxy = new RestAdapter.Builder().setEndpoint(Utils.sWeather_Service_URL_Retro).build()
					.create(WeatherWebServiceProxy.class);

			mGeoNamesServiceProxy = new RestAdapter.Builder().setEndpoint(Utils.Geo_Names_URL).build()
					.create(GeoNamesServiceProxy.class);
			
			mCitylist = new ArrayList<City>();
			
			mCitylist = getCityList();
			
			//mAdapter = new MyCbArrayAdapter(mWeatherView.get().getApplicationContext(), 0, mCitylist);
			adapter = new MyRecyclerViewAdapter(mCitylist);
			
			
		} else
		{
			// Populate the display if a WeatherData object is stored in
			// the WeatherOps instance.
			//if (mCurrentWeatherData != null)
				//mWeatherView.get().displayResults(mCurrentWeatherData);
		}
	}

	/**
	 * Initiate the synchronous weather lookup when the user presses the
	 * "Get Weather" button.
	 */
	public void getCurrentWeather(String location)
	{
		if (mAsyncTask != null)
			// Cancel an ongoing operation to avoid having two
			// requests run concurrently.
			mAsyncTask.cancel(true);

		// Execute the AsyncTask to get the weather without
		// blocking the caller.
		mAsyncTask = new GenericAsyncTask<>(this);
		mAsyncTask.execute(location);
	}

	/**
	 * Get the current weather either from the ContentProvider cache or from the
	 * Weather Service web service.
	 */
	public WeatherData doInBackground(String location)
	{
		try
		{
			// First the cache is checked for the location's
			// weather data.
			WeatherData weatherData = mCache.get(location);

			// If data is in cache return it.
			if (weatherData != null)
				return weatherData;

			// If the location's data wasn't in the cache or was stale,
			// use Retrofit to fetch it from the Weather Service web
			// service.
			else
			{
				Log.v(TAG, location + ": not in cache");

				// Get the weather from the Weather Service.
				weatherData = mWeatherWebServiceProxy.getWeatherData(location);

				// Check to make sure the call to the server succeeded by
				// testing the "name" member to make sure it was
				// initialized.
				if (weatherData.getName() == null || weatherData.getName().equals(""))
					return null;

				WeatherForecast forecast = mWeatherWebServiceProxy
						.getWeatherForecast(weatherData.getName(), Utils.DAYS_AHEAD);
				// Add to cache.

				TimeModifier modifier = mGeoNamesServiceProxy.getModifiers(weatherData.getCoord().getLon(), weatherData
						.getCoord().getLat(), Utils.USERNAME);

				weatherData = DataMediator.modifyWeatherData(forecast, modifier, weatherData);

				mCache.put(location, weatherData);
				return weatherData;
			}
		} catch (Exception e)
		{
			Log.v(TAG, "doInBackground() " + e);
			return null;
		}
	}

	
		/**
	 * Display the results in the UI Thread.
	 */
	public void onPostExecute(WeatherData weatherData, String location)
	{
		if (weatherData == null)
			Utils.showToast(mWeatherView.get().getActivityContext(), "no weather for " + location + " found");
		else
		{
			// Store the weather data in anticipation of runtime
			// configuration changes.
			
			mCurrentWeatherData = weatherData;

			// If the object was found, display the results.
			mWeatherView.get().displayResults(weatherData);
		}

		// Indicate the AsyncTask is done.
		mAsyncTask = null;
	}

	/**
	 * Returns all the citynames that are stored in the  Content Provider.
	 * Used by the array adapter of the listview in Weather Activity.
	 * @return
	 */
	public ArrayList<City> getCityList()
	{
		return mCache.getCityList(mCache.getCursorForAdapter());
	}
	
	
	public void lookupCity()
	{
		// TODO Auto-generated method stub
		
	}

	public void deleteCity(ArrayList<Long> citylist)
	{
		//Just for testing the updater service; TODO : activate it in TimeOutCache
		//Context context = mWeatherView.get().getApplicationContext();
		//mWeatherView.get().getApplicationContext().startService(WeatherService.makeIntent(context, UpdateReceiver.REQUEST_SERVICE));

//		int value = mCache.deleteCities(citylist);
//		Log.d(TAG, "deleted " + String.valueOf(value) + " cities");
		
	}

	public void displayCities(ArrayList<Long> citylist)
	{
		
	}




//	public MyCbArrayAdapter getmAdapter()
//	{
//		return mAdapter;
//	}

	public MyRecyclerViewAdapter getmAdapter()
	{
		return adapter;
	}



	public ArrayList<City> getmCitylist()
	{
		return mCitylist;
	}



	public void setmAdapter(MyRecyclerViewAdapter mAdapter)
	{
		this.adapter = mAdapter;
	}
//	public void setmAdapter(MyCbArrayAdapter mAdapter)
//	{
//		this.mAdapter = mAdapter;
//	}
	
	
}
