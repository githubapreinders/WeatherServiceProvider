package afr.iterson.activities;

import java.util.ArrayList;

import afr.iterson.R;
import afr.iterson.operations.City;
import afr.iterson.operations.MyCbArrayAdapter;
import afr.iterson.operations.WeatherOps;
import afr.iterson.retrofitWeather.WeatherData;
import afr.iterson.utils.GenericActivity;
import afr.iterson.utils.Utils;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

/**
 * The main Activity that prompts the user for a location and then
 * retrieves/displays weather information about this location via various
 * implementations of WeatherServiceSync and WeatherServiceAsync.
 */
public class WeatherActivity extends GenericActivity<WeatherOps.View, WeatherOps> implements WeatherOps.View
{
	/**
	 * Weather entered by the user.
	 */
	protected EditText mEditText;

	/**
	 * Views to hold the Weather Data from Open Weather Map API call.
	 */
	//protected ListView mListView;

	private RecyclerView mRecyclerView;

	private RecyclerView.LayoutManager mLayoutManager;

	
	
	private MyCbArrayAdapter mAdapter;

	private ArrayList<City> mCitylist;

	public static Intent makeIntent(Context context)
	{
		return new Intent(context, WeatherActivity.class);
	}

	/**
	 * Hook method called when a new instance of Activity is created. One time
	 * initialization code goes here, e.g., initializing views.
	 *
	 * @param Bundle
	 *            object that contains saved state information.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Set the content view for this Activity.
		//setContentView(R.layout.chooser_activity);
		setContentView(R.layout.chooser_activity_recyclerview);
		Log.v(TAG, "on create");

		// Initialize the view fields in the activity instance.
		initializeDisplayViewFields();

		// Call up to the special onCreate() method in
		// GenericActivity, passing in the WeatherOps class to
		// instantiate and manage.
		super.onCreate(savedInstanceState, WeatherOps.class, this);
		// mCitylist = new ArrayList<City>();
		// mCitylist.addAll(getOps().getCityList());
		// mAdapter = getOps().setAdapter();
		// mAdapter = new MyCbArrayAdapter(getApplicationContext(), 0,
		// mCitylist);
		//mListView.setAdapter(getOps().getmAdapter());
		mRecyclerView.setAdapter(getOps().getmAdapter());
	}

	/**
	 * Helper method that initializes the views used to display the weather
	 * data.
	 */
	public void initializeDisplayViewFields()
	{
		// Store the EditText that holds the urls entered by the user
		// (if any).
		mEditText = (EditText) findViewById(R.id.editText1);
		//mListView = (ListView) findViewById(R.id.listView1);
		mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
		mLayoutManager = new LinearLayoutManager(this);
		mRecyclerView.setLayoutManager(mLayoutManager);
		

		
		
	}

	/**
	 * Displays the weather data in the list to the user
	 * 
	 * @param weatherList
	 *            List of WeatherData to be displayed, which should not be null.
	 */
	public void displayResults(final WeatherData wd)
	{
		// Get the city and country name.
		if (wd != null)
		{
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					Log.d(TAG, String.valueOf(wd.getGmtOffset()));
					getOps().getmCitylist().add(0, new City(wd.getmName(), wd.getSys().getCountry(), wd.getmId()));
					getOps().getmAdapter().changeData(getOps().getmCitylist());
					mEditText.setText("");
				}
			});
		} else
		{
			Utils.showToast(this, "City not found");
		}

	}

	public void lookupCity(View v)
	{
		Log.d(TAG, "Chooser Activity button lookup click");
		Utils.hideKeyboard(this, mEditText.getWindowToken());
		final String location = mEditText.getText().toString();
		if (location.isEmpty())
		{
			Utils.showToast(this, "Enter a location");
		} else
		{
			getOps().getCurrentWeather(location);
		}

	}

	public void display(View v)
	{
		Log.d(TAG, "making intents for display activity...");
		long[] checkedCities = getOps().getmAdapter().getCheckedCitiesLongArray();
		switch (checkedCities.length)
		{
		case 0:
			break;
		case 2:
		{
			startActivity(Display2Activity.makeIntent(getApplicationContext(), checkedCities));
			break;
		}
		default:
		{
			startActivity(DisplayPagerActivity.makeIntent(getApplicationContext(), checkedCities));
			break;
		}

		}

	}

	@Override
    public void onResume()

	{
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
}
