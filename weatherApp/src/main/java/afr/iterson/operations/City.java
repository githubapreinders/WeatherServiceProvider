package afr.iterson.operations;

import afr.iterson.R;

public class City 
{
	
	String name;
	String country;
	long cityid;
	boolean ischecked;

	public City(String name)
	{
		this.name = name;
		this.ischecked = false;
	}

	public City(String name,  boolean ischecked)
	{
		this.name = name;
		this.ischecked = ischecked;
	}

	public City(String name,  String country)
	{
		this.name = name;
		this.country = country;
		this.ischecked = false;
	}

	public City(String name,  String country, long cityid)
	{
		this.name = name;
		this.country = country;
		this.cityid = cityid;
		this.ischecked = false;
	}

	
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isIschecked()
	{
		return ischecked;
	}

	public int getBgPicture()
	{
		if(isIschecked())
		{
			return R.drawable.bground3;
		}
		else
		{
			return R.drawable.bground2;
		}
	}
	
	public void setIschecked(boolean ischecked)
	{
		this.ischecked = ischecked;
	}

	public String toString()
	{
		return this.name + " checked:" + this.ischecked ;
	}

	public String getCountry()
	{
		return country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public long getCityid()
	{
		return cityid;
	}

	public void setCityid(long cityid)
	{
		this.cityid = cityid;
	}


}