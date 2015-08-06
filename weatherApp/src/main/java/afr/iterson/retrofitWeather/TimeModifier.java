package afr.iterson.retrofitWeather;

import com.google.gson.annotations.SerializedName;

public class TimeModifier
{

	@SerializedName("time")
	String mTime;

	@SerializedName("countryName")
	String mCountryname;

	@SerializedName("sunrise")
	String mSunrisestring;

	@SerializedName("sunset")
	String mSunsetstring;
	
	@SerializedName("gmtOffset")
	double mGmtOffset;


	public TimeModifier(String time, String countryname, String sunrise, String sunset, double gmtoffset)
	{
		this.mTime = time;
		this.mCountryname = countryname;
		this.mSunrisestring = sunrise;
		this.mSunsetstring = sunset;
		this.mGmtOffset = gmtoffset;
	}

	public String getmTime()
	{
		return mTime;
	}

	public String getmCountryname()
	{
		return mCountryname;
	}

	public String getSunsetstring()
	{
		return mSunsetstring;
	}

	public String getSunrisestring()
	{
		return mSunrisestring;
	}

	public double getmGmtOffset()
	{
		return mGmtOffset;
	}

	
	
}
