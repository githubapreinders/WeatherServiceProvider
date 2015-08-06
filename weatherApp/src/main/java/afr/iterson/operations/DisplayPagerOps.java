package afr.iterson.operations;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import retrofit.RestAdapter;
import afr.iterson.R;
import afr.iterson.activities.DisplayPagerActivity;
import afr.iterson.provider.cache.WeatherTimeoutCache;
import afr.iterson.retrofitWeather.GeoNamesServiceProxy;
import afr.iterson.retrofitWeather.WeatherData;
import afr.iterson.retrofitWeather.WeatherWebServiceProxy;
import afr.iterson.utils.ConfigurableOps;
import afr.iterson.utils.ContextView;
import afr.iterson.utils.DataMediator;
import afr.iterson.utils.GenericAsyncTask;
import afr.iterson.utils.GenericAsyncTaskOps;
import afr.iterson.utils.Utils;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class DisplayPagerOps implements ConfigurableOps<DisplayPagerOps.View>,GenericAsyncTaskOps<Long[], Void, ArrayList<WeatherData>>
{

	public static final String TAG = DisplayPagerOps.class.getSimpleName();

	protected WeakReference<DisplayPagerOps.View> mDisplayPagerView;
	
	protected WeatherTimeoutCache mCache;

	private GenericAsyncTask<Long[], Void, ArrayList<WeatherData>, DisplayPagerOps> mAsyncTask;

	
	public interface View extends ContextView
	{
		/**
		 * Finishes the Activity the VideoOps is associated with.
		 */
		void finish();
		void makeFragments(ArrayList<WeatherData> mCurrentWeatherData);
	}

	@Override
	public void onConfiguration(DisplayPagerOps.View view, boolean firstTimeIn)
	{
		mDisplayPagerView = new WeakReference<> ((DisplayPagerOps.View)view);
		
		if (firstTimeIn)
		{
			mCache = new WeatherTimeoutCache(mDisplayPagerView.get().getApplicationContext());
		} else
		{
			Log.d(TAG, "2nd time opsObject");
		}
	}

	/**
	 * Invoked by a display activity; an AsyncTask is used to query the Content Provider
	 * @param cityids
	 */
	public void createFragments(long[] cityids)
	{
		Log.d(TAG, "createFragments");
		mAsyncTask = new GenericAsyncTask<>(this);
		mAsyncTask.execute(ArrayUtils.toObject(cityids));
	}
	

	/**
	 * Retrieves WeatherData after a callback from the Content Observer
	 */
	@Override
	public ArrayList<WeatherData> doInBackground(Long[] param)
	{
		Log.d(TAG, "doInBackground...");
		String[]cityids = DataMediator.getStringArray(ArrayUtils.toPrimitive(param));
		return mCache.getRowsForCityIds(cityids);
	}

		
	/**
	 * Returns the WeatherData to the main activity where two fragments are being made of it.
	 */
	@Override
	public void onPostExecute(ArrayList<WeatherData> result, Long[] param)
	{
		Log.d(TAG, "onPostExecute...");
		mDisplayPagerView.get().makeFragments(result);
	}
			
		

	public WeatherData getFromCache(long cityid)
	{
		ArrayList<WeatherData> list = mCache.getRowsForCityIds(new String[]{String.valueOf(cityid)});
		return list.get(0);
	}

	
	
}
