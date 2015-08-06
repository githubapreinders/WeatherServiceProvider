package afr.iterson.retrofitWeather;

import java.util.List;

import afr.iterson.retrofitWeather.WeatherData.Weather;

import com.google.gson.annotations.SerializedName;

public class WeatherForecast
{

/**
 * Json representation
 * {"city":{"id":2757345,"name":"Delft","coord":{"lon":4.35556,"lat":52.006672},"country":"NL","population":0},"cod":"200","message":0.0344,"cnt":2,
 * "list":[{"dt":1435489200,"temp":{"day":292.72,"min":288.82,"max":293.13,"night":288.82,"eve":290.26,"morn":293.13},"pressure":1034.31,"humidity":79,"weather":[{"id":500,"main":"Rain","description":"light rain","icon":"10d"}],"speed":4.28,"deg":205,"clouds":92},
 * {"dt":1435575600,"temp":{"day":289.8,"min":287.67,"max":289.8,"night":287.67,"eve":288.91,"morn":288.91},"pressure":1035.45,"humidity":90,"weather":[{"id":801,"main":"Clouds","description":"few clouds","icon":"02d"}],"speed":3.67,"deg":331,"clouds":12}]}	
 */
	
	@SerializedName("cod")
	private String mCod;
	
	@SerializedName("city")
	private City mCity;
	
	@SerializedName("list")
	private List<OneDayForecasts> onedayForecasts;
	
	public WeatherForecast(String resultcode, City city, List<OneDayForecasts> list)
	{
		this.mCod = resultcode;
		this.mCity = city;
		this.onedayForecasts = list;
	}
	
	public List<OneDayForecasts> getOnedayForecasts()
	{
		return onedayForecasts;
	}

	
	
	
	private class City
	{
		@SerializedName("id")
		private String mId;
		
		@SerializedName("name")
		private String mName;
		
		public City(String id, String name)
		{
			this.mId = id;
			this.mName = name;
		}

		public String getmId()
		{
			return mId;
		}

		public String getmName()
		{
			return mName;
		}
	}
	
	
	
	
	
	public class OneDayForecasts
	{
		@SerializedName("dt")
		private String dt;
		
		@SerializedName("temp")
		private Temp mTemp;

		@SerializedName("weather")
		private List<Weather> mWeather;

		public OneDayForecasts(String dt, Temp temp, List<Weather> weather)
		{
			this.dt = dt;
			this.mTemp = temp;
			this.mWeather = weather;
		}

		public String getDt()
		{
			return dt;
		}

		public Temp getmTemp()
		{
			return mTemp;
		}

		public List<Weather> getmWeather()
		{
			return mWeather;
		}
		
	}
	
	
	private class Temp
	{
		@SerializedName("day")
		private double mDay;
		
		public Temp(double averagedaytemp)
		{
			this.mDay = averagedaytemp;
		}

		public double getmDay()
		{
			return mDay;
		}
		
	}


		
}
