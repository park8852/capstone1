package com.example.map0329;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient fusedLocationClient;

    private LocationRequest locationRequest; // 위치 요청의 구성(업데이트 간격, 우선순위 등)을 설정
    private LocationCallback locationCallback; // 위치 업데이트가 있을 때 호출될 콜백을 정의

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 위치 서비스 클라이언트를 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        // 구글맵
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // 위치 요청과 콜백을 설정
        createLocationRequest();
        createLocationCallback();

        // 위치 권한 요청을 시작
        requestLocationPermission();
    }


    private void createLocationRequest() { // 위치 요청의 구성(업데이트 간격, 우선순위 등)을 설정
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(3000); // 위치 업데이트 요청 간격 (3초)
        locationRequest.setFastestInterval(1500); // 가장 빠른 업데이트 간격
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // 정확도 우선
    }

    private boolean checkLocationPermission() { // 위치 권한 확인
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    // 위치 업데이트가 발생할 때 호출될 LocationCallback을 정의. 이 콜백에서는 위치 정보가 null이 아닐 경우, 받아온 위치 정보(위도와 경도)를 로그에 기록
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // 로그에 위도와 경도를 출력
                    Log.d("MainActivity", "위도: " + location.getLatitude() + ", 경도: " + location.getLongitude());
                }
            }
        };
    }

    private void getLastLocation() { // 사용자의 마지막 알려진 위치를 가져옴
        if (!checkLocationPermission()) {
            // 권한 요청 로직 추가 (필요한 경우)
            // ActivityCompat#requestPermissions 호출 등
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // 로그에 위도와 경도를 출력합니다.
                            Log.d("MainActivity", "위도: " + location.getLatitude() + ", 경도: " + location.getLongitude());
                        }
                    }
                });
    }

    private void requestLocationPermission() { // 여기서 권한을 요청하고
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, // 권한 요청에 대한 추가 설명이 필요한지 확인
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // 여기에 사용자에게 권한이 필요한 이유를 설명하는 UI를 제공
            } else {
                ActivityCompat.requestPermissions(this, // 권한 요청
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            getLastLocation(); // 사용자의 마지막 알려진 위치를 가져옴
        }
    }

    @Override// 여기서 권한 요청에 대한 응답을 처리
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation(); // 사용자의 마지막 알려진 위치를 가져옴
            } else {
                // 권한이 거부되었습니다. 적절한 조치를 취합니다.
            }
        }
    }


    private void startLocationUpdates() { // 위치 업데이트를 시작합니다. 이때, 설정한 locationRequest와 locationCallback을 인자로 전달합니다.
        if (!checkLocationPermission()) {
            // 권한 요청 로직 추가 (필요한 경우)
            // ActivityCompat#requestPermissions 호출 등
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */);
    }
    private void stopLocationUpdates() { // 위치 업데이트를 중지
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onResume() { // 앱이 사용자에게 보일 때
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() { // 보이지 않을 때
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
        } else {
            // 권한 요청
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission") Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastKnownLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), 19));
        }

        // 좌표 설정
        LatLng p = new LatLng(36.32206, 127.3674);
        LatLng y = new LatLng(36.32328, 127.3663);
        LatLng wg = new LatLng(36.32323, 127.3655);
        LatLng am = new LatLng(36.32262, 127.3651);
        LatLng b = new LatLng(36.32119, 127.3661);
        LatLng w = new LatLng(36.31959, 127.3660);
        LatLng a = new LatLng(36.31883, 127.3664);
        LatLng j = new LatLng(36.31823, 127.3663);
        LatLng h = new LatLng(36.31769, 127.3673);
        LatLng mc = new LatLng(36.31752, 127.3669);
        LatLng c = new LatLng(36.31756, 127.3678);
        LatLng s = new LatLng(36.31801, 127.3682);
        LatLng sp = new LatLng(36.31921, 127.3669);

        // 마커 추가
        mMap.addMarker(new MarkerOptions().position(p).title("21세기관(P)"));
        mMap.addMarker(new MarkerOptions().position(y).title("예술관(Y)"));
        mMap.addMarker(new MarkerOptions().position(wg).title("원예실습동(WG)"));
        mMap.addMarker(new MarkerOptions().position(am).title("아펜젤러기념관(AM)"));
        mMap.addMarker(new MarkerOptions().position(b).title("백산관(B)"));
        mMap.addMarker(new MarkerOptions().position(w).title("우남관(W)"));
        mMap.addMarker(new MarkerOptions().position(a).title("아펜젤러관(A)"));
        mMap.addMarker(new MarkerOptions().position(j).title("자연과학관(J)"));
        mMap.addMarker(new MarkerOptions().position(h).title("하워드관(H)"));
        mMap.addMarker(new MarkerOptions().position(mc).title("미래창조관(MC)"));
        mMap.addMarker(new MarkerOptions().position(c).title("정보과학관(C)"));
        mMap.addMarker(new MarkerOptions().position(s).title("소월관(S)"));
        mMap.addMarker(new MarkerOptions().position(sp).title("SMART배재관(SP) "));




    }

}












