package afr.iterson.operations;

import java.util.List;

import afr.iterson.activities.WeatherFragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

public class MyPagerAdapter extends FragmentStatePagerAdapter
{
	private static final String TAG = MyPagerAdapter.class.getSimpleName();

	public List<WeatherFragment> fragments;

	public MyPagerAdapter(android.support.v4.app.FragmentManager fm, List<WeatherFragment> fragments)
	{
		super(fm);
		this.fragments = fragments;
	}

	
	
	@Override
	public int getCount()
	{
		return this.fragments.size();
	}

	@Override
	public WeatherFragment getItem(int position)
	{
		return this.fragments.get(position);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		super.destroyItem(container, position, object);
	}

	public int getPosition(WeatherFragment fragment)
	{
		return fragments.indexOf(fragment);
	}
	
	
	
	/**
	 * Items have been added or deleted so all the views have to be
	 * reprocessed;
	 */
	@Override
	public int getItemPosition(Object object)
	{
		return MyPagerAdapter.POSITION_NONE;
	}

	@Override
	public void notifyDataSetChanged()
	{
		super.notifyDataSetChanged();
	}



	}
