package com.sla.codurs.chas.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnPanListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.popup.Popup;
import com.esri.android.map.popup.PopupContainer;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Transformation2D;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.Symbol;
import com.sla.codurs.chas.HTTP.GetAddressSearchRequest;
import com.sla.codurs.chas.HTTP.GetChasRequest;
import com.sla.codurs.chas.R;
import com.sla.codurs.chas.fragments.FragmentCodursPager;
import com.sla.codurs.chas.utils.AddressAdapter;
import com.sla.codurs.chas.utils.StaticObjects;


public class BaseActivity extends Activity {
    MapView mMapView;
    LocationDisplayManager ls;
    int addressSet=1;
    Button searchBtn;
    ListView result;
    ProgressDialog dialog;

    private EditText searchQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        searchBtn=(Button)findViewById(R.id.searchBtn);
        searchQuery=(EditText)findViewById(R.id.searchQuery);
        result=(ListView)findViewById(R.id.addressResultList);

        mMapView = (MapView) findViewById(R.id.map);
        ArcGISRuntime.setClientId("j9r0J2JIy8FFFfB8");
        mMapView.addLayer(new ArcGISTiledMapServiceLayer("http://www.onemap.sg/ArcGIS/rest/services/BASEMAP/MapServer"));

        ls = mMapView.getLocationDisplayManager();
        ls.start();

        result.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getBaseContext(),"Selected"+StaticObjects.addresses.get(position).getTitle(),Toast.LENGTH_SHORT).show();

                GetChasRequest searchRequest= new GetChasRequest("1","1","1","1");
                new ChasBackgroundTask().execute(searchRequest,searchRequest);
            }
        });
        searchQuery.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                findViewById(R.id.resultFrame).setVisibility(View.VISIBLE);
                return false;
            }
        });

        initMapListeners();
        plotChas();




    }



    public void startSearch(View v){
        Log.i("Start","click");
        StaticObjects.addressEnd=false;
        StaticObjects.addresses=null;
        GetAddressSearchRequest searchRequest= new GetAddressSearchRequest(searchQuery.getText().toString(),addressSet);
        new BackgroundTask().execute(searchRequest,searchRequest);
    }

    public void displayResult(){
       AddressAdapter adapter= new AddressAdapter(getBaseContext(),R.layout.address_list_layout,StaticObjects.addresses);
       result.setAdapter(adapter);
       findViewById(R.id.resultFrame).setVisibility(View.VISIBLE);

    }
    public void displayNoResult(){
        result.setAdapter(null);
        findViewById(R.id.resultFrame).setVisibility(View.GONE);
    }



    public void initMapListeners(){
        ls.setLocationListener(new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
//                Log.i("Extends Location Change", "" + mMapView.getExtent().getPoint(0));
//                Log.i("Extends Location Change", "" + mMapView.getExtent().getPoint(1));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
//                Log.i("Extends Location Change", "" + mMapView.getExtent().getPoint(0));
//                Log.i("Extends Location Change", "" + mMapView.getExtent().getPoint(1));
            }

            @Override
            public void onProviderEnabled(String provider) {
//                Log.i("Extends Location Change", "" + mMapView.getExtent().getPoint(0));
//                Log.i("Extends Location Change", "" + mMapView.getExtent().getPoint(1));
            }

            @Override
            public void onProviderDisabled(String provider) {
//                Log.i("Extends Location Change", "" + mMapView.getExtent().getPoint(0));
//                Log.i("Extends Location Change", "" + mMapView.getExtent().getPoint(1));
            }
        });

        mMapView.setOnSingleTapListener(new OnSingleTapListener() {
            @Override
            public void onSingleTap(float v, float v2) {
               // Log.i("Extends", "" + mMapView.getExtent().getPoint(0));
               // Log.i("Extends", "" + mMapView.getExtent().getPoint(1));
            }
        });




    }

    public void zoomToGps(View v){
        mMapView.zoomTo(ls.getPoint(), 14);
    }
    public void getChasAroundGPS(View v){
        String xMin=Double.toString(ls.getLocation().getLatitude());
        String yMin=Double.toString(ls.getLocation().getLatitude());
        String xMax=Double.toString(ls.getLocation().getLatitude());
        String yMax=Double.toString(ls.getLocation().getLongitude());


        GetChasRequest searchRequest= new GetChasRequest(xMin,yMin,xMax,yMax);
        new BackgroundTask().execute(searchRequest, searchRequest);
    }

    public void plotChas(){

        GraphicsLayer chasLayer= new GraphicsLayer();

        for(int i=0;i<StaticObjects.chases.size();i++){
            PictureMarkerSymbol icon= new PictureMarkerSymbol(getBaseContext(),getResources().getDrawable(R.drawable.chas_logo));
            PopupContainer popupContainer= new PopupContainer(mMapView);
            Graphic graphic = new Graphic(new Point(StaticObjects.chases.get(i).getX(),StaticObjects.chases.get(i).getY()),icon);
            Popup popup = chasLayer.createPopup(mMapView, 0, graphic);
            popupContainer.addPopup(popup);
            chasLayer.addGraphic(graphic);
        }


        mMapView.addLayer(chasLayer);
    }


    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    protected void onResume() {
        super.onResume();
        mMapView.unpause();
    }



    private class ChasBackgroundTask extends AsyncTask<Runnable, Integer, Long> {

        @Override
        protected void onPostExecute(Long result) {


        }

        @Override
        protected void onPreExecute() {
//            dialog = ProgressDialog.show(getBaseContext(), "",
//                    "Loading. Please wait...", true);
            //Toast.makeText(getBaseContext(), "Loading...", Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        @Override
        protected Long doInBackground(Runnable... task) {

            for(int i=0; i<task.length;i++)
            {
                task[i].run();

                if (isCancelled()) break;
            }
            return null;
        }
    }

    private class BackgroundTask extends AsyncTask<Runnable, Integer, Long> {

        @Override
        protected void onPostExecute(Long result) {
//            dialog.dismiss();
            if(StaticObjects.addressEnd==false)
            {
                addressSet++;
                GetAddressSearchRequest searchRequest= new GetAddressSearchRequest(searchQuery.getText().toString(),addressSet);
                new BackgroundTask().execute(searchRequest,searchRequest);
            }
            else
            {
                StaticObjects.addressEnd=true;
                addressSet=1;
                if(StaticObjects.addresses==null)
                {
                    Toast.makeText(getBaseContext(),"No result found",Toast.LENGTH_SHORT).show();
                    displayNoResult();
                }
                else {
                    Log.i("ENDEd",""+StaticObjects.addresses.size());

                    displayResult();
                }
            }

        }

        @Override
        protected void onPreExecute() {
//            final ProgressDialog progDailog = ProgressDialog.show(getBaseContext(), "Progress_bar or give anything you want",
//                    "Give message like ....please wait....", true);
            //Toast.makeText(getBaseContext(), "Loading...", Toast.LENGTH_LONG).show();
            super.onPreExecute();
        }

        @Override
        protected Long doInBackground(Runnable... task) {
            task[0].run();
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        findViewById(R.id.resultFrame).setVisibility(View.GONE);
    }
}
