package com.example.hellosearch.model;


import android.util.Log;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import java.util.ArrayList;

/**
 * Created by Madhav Chhura on 8/30/15.
 */
public class Yelp {

    final String LOGTAG = "Yelp";
    private final String API_HOST = "api.yelp.com";
    private final int SEARCH_LIMIT = 19;
    private final String SEARCH_PATH = "/v2/search";
    private final String BUSINESS_PATH = "/v2/business";

    private final String CONSUMER_KEY = "PLV3AX-PtJsOzyZ5IfuEBg";
    private final String CONSUMER_SECRET = "SRA1euYDidW8sa3f87Xy4_ed0Ng";
    private final String TOKEN = "Bj9u37Yu6I70txiaGa-JeL3ZKIjP2N9D";
    private final String TOKEN_SECRET = "3O-OSMyFE4jsQnNtV7MJ0bD4rBE";

    private double mLatitude, mLongitude;
    private String mTerm;
    private int offset = 0;
    private Search mySearch;

    OAuthService service;
    Token accessToken;

    public Yelp(String term, double latitude, double longitude) {
        this.mTerm = term;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.service = new ServiceBuilder()
                .provider(TwoStepOAuth.class)
                .apiKey(this.CONSUMER_KEY)
                .apiSecret(this.CONSUMER_SECRET)
                .build();
        this.accessToken = new Token(this.TOKEN, this.TOKEN_SECRET);
        System.out.println(accessToken);
        mySearch = new Search(term, mLatitude, mLongitude);
    }

    private OAuthRequest createOAuthRequest(String path) {
        OAuthRequest request = new OAuthRequest(Verb.GET, "http://" + API_HOST + path);
        return request;
    }

    public String searchForBusinessesByLocation() {
        OAuthRequest request = createOAuthRequest(SEARCH_PATH);
        request.addQuerystringParameter("term", mySearch.input.query);
        request.addQuerystringParameter("ll", String.valueOf(mLatitude) + "," + String.valueOf(mLongitude));
//        request.addQuerystringParameter("location","San Francisco");
        request.addQuerystringParameter("offset", String.valueOf(offset));
        request.addQuerystringParameter("limit", String.valueOf(SEARCH_LIMIT));
        return sendRequestAndGetResponse(request);
    }

    //Could be used for searching businesses by an Id.
    public String searchByBusinessId(String businessID) {
        OAuthRequest request = createOAuthRequest(BUSINESS_PATH + "/" + businessID);
        return sendRequestAndGetResponse(request);
    }

    private String sendRequestAndGetResponse(OAuthRequest request) {
        Log.d(LOGTAG, "Querying " + request.getCompleteUrl() + " ...");
        this.service.signRequest(this.accessToken, request);
        Response response = request.send();
        return response.getBody();
    }

    public Search parseResponse (String temp) {
        String searchResponseJSON = temp;
        Log.d(LOGTAG,searchResponseJSON);

        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
            response = (JSONObject) parser.parse(searchResponseJSON);
        } catch (ParseException pe) {
            Log.d(LOGTAG, "parseResponse - Error: could not parse JSON response:");
            Log.d(LOGTAG, searchResponseJSON);
        }

        JSONArray businesses = (JSONArray) response.get("businesses");
        if(businesses == null)
            Log.d(LOGTAG, "No businesses found");
        else {
            mySearch.businesses = new ArrayList<>(businesses.size());

            for (int i = 0; i < businesses.size(); i++) {
                JSONObject firstBusiness = (JSONObject) businesses.get(i);
                Business business = new Business();
                business.businessName = firstBusiness.get("name").toString();
                business.image_url = firstBusiness.get("image_url").toString();
                business.business_id = firstBusiness.get("id").toString();
                business.url = firstBusiness.get("url").toString();

                //Sometimes the display phone number is not available.
                if (firstBusiness.get("display_phone") != null)
                    business.display_phone = firstBusiness.get("display_phone").toString();
                else
                    business.display_phone = "Number Not Available";
                business.rating_img_url = firstBusiness.get("rating").toString();
                mySearch.businesses.add(business);
            }
        }
        return mySearch;
    }

    public Search nextPage(){
        offset = offset + SEARCH_LIMIT;
        return parseResponse(this.searchForBusinessesByLocation());
    }

}

