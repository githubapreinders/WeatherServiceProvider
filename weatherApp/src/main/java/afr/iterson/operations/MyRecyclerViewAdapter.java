package afr.iterson.operations;

import java.util.ArrayList;
import java.util.HashMap;

import afr.iterson.R;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>
{
	public static final String TAG = MyRecyclerViewAdapter.class.getSimpleName();
	public ArrayList<City> group;
	private static int MAX_CHECKED_ITEMS = 4;
	private String placeholder = "";
	private boolean deleted = false;
	private String[] checkedCities = new String[MAX_CHECKED_ITEMS];
	private HashMap<Long,City> checkedCitiesMap = new HashMap<Long,City>();

	public MyRecyclerViewAdapter( ArrayList<City> group)
	{
		super();
		this.group = group;
	}

	
	public class ViewHolder extends RecyclerView.ViewHolder
	{
		TextView tv;
		CheckBox cb;
		LinearLayout ll;
		TextView tvcountry;

		public ViewHolder(View v)
		{
			super(v);
			this.tv = (TextView) v.findViewById(R.id.listitem_property);
			this.cb = (CheckBox) v.findViewById(R.id.checkBox1);
			this.ll = (LinearLayout) v.findViewById(R.id.ll_listitem_checkbox);
			this.tvcountry = (TextView) v.findViewById(R.id.listitem_country);
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int arg1)
	{
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_with_checkbox, parent,false);
		ViewHolder vh = new ViewHolder(v);
		return vh;
	}

	
	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		final City city  = group.get(position);
		holder.tv.setText(city.getName());
		holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				View parent = (View) buttonView.getParent();
				final LinearLayout ll = (LinearLayout) parent.findViewById(R.id.ll_listitem_checkbox);
				if (isChecked)
				{
					ll.setBackgroundResource(R.drawable.bground3);
					city.setIschecked(true);
					checkedCitiesMap.put(city.getCityid(), city);

				} else
				{
					ll.setBackgroundResource(R.drawable.bground2);
					city.setIschecked(false);
					checkedCitiesMap.remove(city.getCityid());
				}
			}
		});
		holder.tvcountry.setText(city.getCountry());
		
	}

	@Override
	public int getItemCount()
	{
		return group.size();
	}

	public void add(int position, City item) {
	    group.add(position, item);
	    notifyItemInserted(position);
	  }

	  public void remove(City item) {
	    int position = group.indexOf(item);
	    group.remove(position);
	    notifyItemRemoved(position);
	  }

	
	public ArrayList<String> getCheckedCities()
	{
		ArrayList<String> result = new ArrayList<String>();
		for (City c : group)
		{
			if (c.isIschecked())
			{
				result.add(c.getName());
			}
		}
		Log.d(TAG, "Amount of checked cities: " + result.size());
		return result;
	}

	public ArrayList<Long> getCheckedCitiesLongs()
	{
		ArrayList<Long> result = new ArrayList<Long>();
		for (City c : group)
		{
			if (c.isIschecked())
			{
				result.add(c.getCityid());
			}
		}
		Log.d(TAG, "Amount of checked cities: " + result.size());
		return result;
	}

	public long[] getCheckedCitiesLongArray()
	{
		long[] returnarray = new long[checkedCitiesMap.size()];
		int counter=0;
		for(Long value : checkedCitiesMap.keySet())
		{
			returnarray[counter] = value.longValue();
			counter++;
		}
		Log.d(TAG, returnarray.toString());
		return returnarray;
	}

	

	public String showArray()
	{
		String returnvalue = "";
		for (int i = 0; i < MAX_CHECKED_ITEMS; i++)
		{
			returnvalue += checkedCities[i] + ", ";
		}
		return returnvalue;
	}

	public void changeData(ArrayList<City> data)
	{
		this.group.clear();
		this.group.addAll(data);
		notifyDataSetChanged();
	}

	public ArrayList<String> getAllCities()
	{
		ArrayList<String> list = new ArrayList<String>();
		for (City c : group)
		{
			list.add(c.getName());
		}
		return list;
	}

	
	
	
	}
