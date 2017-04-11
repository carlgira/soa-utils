package com.carlgira.https.client.test;

import com.carlgira.https.client.*;
import static org.junit.Assert.*;

/**
 * Created by cagirald on 05/04/2017.
 */
public class Test {


    @org.junit.Test
    public void testService(){
        CountriesPortService countriesPortService = new CountriesPortService();
        CountriesPort countriesPort = countriesPortService.getCountriesPortSoap11();

        GetCountryRequest getCountryRequest = new GetCountryRequest();
        getCountryRequest.setName("Spain");

        GetCountryResponse getCountryResponse = countriesPort.getCountry(getCountryRequest);
        Country country = getCountryResponse.getCountry();

        assertNotNull(country);
        System.out.println("\nGetCountryResponse --> Name: " + country.getName() + ", Population: " + country.getPopulation() + ", Currency: " + country.getCurrency() + ",  Capital: " + country.getCapital() + "\n");
    }


}
