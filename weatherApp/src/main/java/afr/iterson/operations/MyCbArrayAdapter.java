package afr.iterson.operations;

import java.util.ArrayList;
import java.util.HashMap;

import afr.iterson.R;
import android.content.Context;
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

public class MyCbArrayAdapter extends ArrayAdapter<City>
{
	public static final String TAG = MyCbArrayAdapter.class.getSimpleName();
	public ArrayList<City> group;
	private final Context context;
	private static int MAX_CHECKED_ITEMS = 4;
	private String placeholder = "";
	private boolean deleted = false;
	private String[] checkedCities = new String[MAX_CHECKED_ITEMS];
	private HashMap<Long,City> checkedCitiesMap = new HashMap<Long,City>();

	public MyCbArrayAdapter(Context context, int textViewResourceId, ArrayList<City> group)
	{
		super(context, textViewResourceId, group);
		this.group = group;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final City city = group.get(position);
		ViewHolder holder;
		TextView tv;
		TextView tvcountry;
		CheckBox cb;
		LinearLayout ll;
		if (convertView == null)
		{
			LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.list_item_with_checkbox, null);
			tv = (TextView) convertView.findViewById(R.id.listitem_property);
			cb = (CheckBox) convertView.findViewById(R.id.checkBox1);
			ll = (LinearLayout) convertView.findViewById(R.id.ll_listitem_checkbox);
			tvcountry = (TextView) convertView.findViewById(R.id.listitem_country);
			
			holder = new ViewHolder(tv, cb, ll, tvcountry);
			convertView.setTag(holder);
		} else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.cb.setTag(group.get(position));
//		holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
//		{
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
//			{
//				if (deleted)
//				{
//					deleted = false;
//					return;
//				}
//				View parent = (View) buttonView.getParent();
//				final LinearLayout ll = (LinearLayout) parent.findViewById(R.id.ll_listitem_checkbox);
//				if (isChecked)
//				{
//					city.setIschecked(true);
//					addCheckedItem(city);
//					ll.setBackgroundResource(R.drawable.bground3);
//
//				} else
//				{
//					city.setIschecked(false);
//					ll.setBackgroundResource(R.drawable.bground2);
//					if (!(city.getName().equals(placeholder)))
//					{
//						removeCheckedItem(city);
//					}
//					placeholder = "";
//				}
//			}
//
//		});
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
		holder.tv.setText(city.name);
		holder.cb.setChecked(city.ischecked);
		holder.ll.setBackgroundResource(city.getBgPicture());
		holder.tvcountry.setText(city.country);
		return convertView;
	}

	public class ViewHolder
	{
		TextView tv;
		CheckBox cb;
		LinearLayout ll;
		TextView tvcountry;

		public ViewHolder(TextView t, CheckBox c, LinearLayout l,TextView t2)
		{
			this.tv = t;
			this.cb = c;
			this.ll = l;
			this.tvcountry = t2;
		}
	}

	public void addCheckedItem(City city)
	{
		/**
		 * If the array is full of values ( = if we have a value at the last
		 * position) Uncheck this item in the DataSet
		 */
		if (checkedCities[MAX_CHECKED_ITEMS - 1] != null)
		{
			String name = checkedCities[MAX_CHECKED_ITEMS - 1];
			for (City c : group)
			{
				if (c.getName().equals(name))
				{
					c.setIschecked(false);
					placeholder = name;
					notifyDataSetChanged();
					break;
				}
			}
		}
		/**
		 * Move everyitem in the array one place forward so that the first item
		 * becomes available; The last item will be overwritten and has already
		 * been unchecked
		 */
		for (int i = MAX_CHECKED_ITEMS - 1; i > 0; i--)
		{
			if (checkedCities[i - 1] != null)
			{
				checkedCities[i] = checkedCities[i - 1];
			}
		}

		/**
		 * The first item in the array gets the new value; With each new
		 * addition this value gets a higher position in the array until it
		 * falls out.
		 */
		checkedCities[0] = city.getName();
		Log.i(TAG, showArray());
	}

	private void removeCheckedItem(City city)
	{
		String name = city.getName();
		int index = 0;
		for (String s : checkedCities)
		{
			if (s != null && s.equals(name))
			{
				break;
			}
			index++;
		}
		if(index>MAX_CHECKED_ITEMS-1){index--;}
		checkedCities[index] = null;

		for (int i = index; i < MAX_CHECKED_ITEMS - 1; i++)
		{
			checkedCities[i] = checkedCities[i + 1];
		}
		checkedCities[MAX_CHECKED_ITEMS - 1] = null;
		Log.e(TAG, showArray());
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
//		ArrayList<Long> list = new ArrayList<Long>();
//		for (City c : group)
//		{
//			if (c.isIschecked())
//			{
//				list.add(c.getCityid());
//			}
//		}
//		Log.d(TAG, "Amount of checked cities: " + list.size());
//		long[] result = new long[list.size()];
//		int counter = 0;
//		for (Long l : list)
//		{
//			result[counter] = l;
//			counter++;
//		}
//		Log.d(TAG, result.toString());
		
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

	public void removeCitiesFromAdapter()
	{
		ArrayList<Integer> indexlist = new ArrayList<Integer>();
		for (City c : group)
		{
			if (c.isIschecked())
			{
				indexlist.add(group.indexOf(c));
			}
		}
		for (Integer index : indexlist)
		{
			group.remove((int) index);
		}
		deleted = true;
		notifyDataSetChanged();
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
