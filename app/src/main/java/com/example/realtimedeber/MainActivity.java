package com.example.realtimedeber;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.realtimedeber.ml.ModelBandera;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.text.Text;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import Adapter.PaisAdapter;
import Models.Pais;
import Utiles.ErrorLog;
import camerax.CameraConnectionFragment;
import camerax.ImageUtils;

public class MainActivity extends AppCompatActivity implements OnSuccessListener<Text>,
        OnFailureListener, ImageReader.OnImageAvailableListener {

    TextView txtResults;
    ArrayList<String> permisosNoAprobados;
    Bitmap mSelectedImage;

    RecyclerView paisrecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Permisos
        ArrayList<String> permisos_requeridos = new ArrayList<String>();
        permisos_requeridos.add(Manifest.permission.CAMERA);

        //Indica el resultado de la aplicación
        txtResults = findViewById(R.id.txtresults);

        permisosNoAprobados  = getPermisosNoAprobados(permisos_requeridos);
        requestPermissions(permisosNoAprobados.toArray(new String[permisosNoAprobados.size()]),
                100);

    }

    //Permisos no aprobados
    public ArrayList<String> getPermisosNoAprobados(ArrayList<String>  listaPermisos) {
        ArrayList<String> list = new ArrayList<String>();
        Boolean habilitado;


        if (Build.VERSION.SDK_INT >= 23)
            for(String permiso: listaPermisos) {
                if (checkSelfPermission(permiso) != PackageManager.PERMISSION_GRANTED) {
                    list.add(permiso);
                    habilitado = false;
                }else
                    habilitado=true;
                /*if(permiso.equals(Manifest.permission.CAMERA))
                    btnCamara.setEnabled(habilitado);
                else if (permiso.equals(Manifest.permission.MANAGE_EXTERNAL_STORAGE)  ||
                        permiso.equals(Manifest.permission.READ_EXTERNAL_STORAGE))
                    btnGaleria.setEnabled(habilitado);*/
            }


        return list;
    }

    //Incovar a la camara
    public void abrirCamera (View view){
        //Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //startActivityForResult(intent, REQUEST_CAMERA);
        this.setFragment();
        //paisrecycler = (RecyclerView) findViewById(R.id.recyclerPaises);
        paisrecycler.setHasFixedSize(true);
        paisrecycler.setLayoutManager(new LinearLayoutManager(this));
        paisrecycler.setItemAnimator(new DefaultItemAnimator());

    }

    int previewHeight = 0,previewWidth = 0;
    int sensorOrientation;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void setFragment() {
        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        String cameraId = null;
        try {
            cameraId = manager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        CameraConnectionFragment fragment;
        CameraConnectionFragment camera2Fragment =
                CameraConnectionFragment.newInstance(
                        new CameraConnectionFragment.ConnectionCallback() {
                            @Override
                            public void onPreviewSizeChosen(final Size size, final int rotation) {
                                previewHeight = size.getHeight();    previewWidth = size.getWidth();
                                sensorOrientation = rotation - getScreenOrientation();
                            }
                        },
                        this,   R.layout.camera_fragment,            new Size(640, 480));

        camera2Fragment.setCamera(cameraId);
        fragment = camera2Fragment;
        getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    //Valores de la orientacion de la camara
    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

    //Mostrar el resultado
    @Override
    public void onSuccess(Text text) {
        List<Text.TextBlock> blocks = text.getTextBlocks();
        String resultados="";
        if (blocks.size() == 0) {
            resultados = "No hay Texto";
        }else{
            for (int i = 0; i < blocks.size(); i++) {
                List<Text.Line> lines = blocks.get(i).getLines();
                for (int j = 0; j < lines.size(); j++) {
                    List<Text.Element> elements = lines.get(j).getElements();
                    for (int k = 0; k < elements.size(); k++) {
                        resultados = resultados + elements.get(k).getText() + " ";
                    }
                }
            }
            resultados=resultados + "\n";
        }
        txtResults.setText(resultados);
    }

    //Error de procesamiento de imagen
    @Override
    public void onFailure(@NonNull Exception e) { txtResults.setText("Error al procesar imagen"); }

    //
    public void PersonalizedModel(View v) {
        try {
            ModelBandera model = ModelBandera.newInstance(getApplicationContext());
            TensorImage image = TensorImage.fromBitmap(mSelectedImage);

            ModelBandera.Outputs outputs = model.process(image);

            List<Category> probability = outputs.getProbabilityAsCategoryList();
            Collections.sort(probability, new CategoryComparator());

            String res="";
            for (int i = 0; i < probability.size(); i++) {
                res = res + probability.get(i).getLabel() +  " " +  probability.get(i).getScore()*100 + " % \n";
            }

            txtResults.setText(res);

            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(this);
            String url ="http://www.geognos.com/api/en/countries/info/"+probability.get(0).getLabel()+".json";

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            ArrayList<Pais> paises = new ArrayList<Pais> ();
                            try {
                                JSONObject JSONOBJECTpais = new JSONObject(response);
                                JSONObject JSONOBJECTpais2 = JSONOBJECTpais.getJSONObject("Results");
                                Iterator< String > codigosPaises = JSONOBJECTpais2.keys();
                                while (codigosPaises.hasNext())
                                    paises.add(new Pais(JSONOBJECTpais2.getJSONObject(codigosPaises.next())));

                                PaisAdapter adapatorPais = new PaisAdapter(getApplicationContext(), paises);
                                paisrecycler.setAdapter(adapatorPais);
                            } catch (JSONException e) {
                                //ErrorLog.info(e.getMessage());
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    ErrorLog.error(error.getMessage());
                    Toast.makeText(getApplicationContext(),  error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            // Add the request to the RequestQueue.
            queue.add(stringRequest);


            model.close();
        } catch (IOException e) {
            txtResults.setText("Error al procesar Modelo");
        }
    }
    private boolean isProcessingFrame = false;
    private byte[][] yuvBytes = new byte[3][];
    private int[] rgbBytes = null;
    private int yRowStride;
    private Runnable postInferenceCallback;
    private Runnable imageConverter;
    private Bitmap rgbFrameBitmap;

    //Control de la imagen
    @Override
    public void onImageAvailable(ImageReader imageReader) {
        if (previewWidth == 0 || previewHeight == 0)           return;
        if (rgbBytes == null)    rgbBytes = new int[previewWidth * previewHeight];
        try {
            final Image image = imageReader.acquireLatestImage();
            if (image == null)    return;
            if (isProcessingFrame) {           image.close();            return;         }
            isProcessingFrame = true;
            final Image.Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);
            yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();
            imageConverter =  new Runnable() {
                @Override
                public void run() {
                    ImageUtils.convertYUV420ToARGB8888( yuvBytes[0], yuvBytes[1], yuvBytes[2], previewWidth,  previewHeight,
                            yRowStride,uvRowStride, uvPixelStride,rgbBytes);
                }
            };
            postInferenceCallback =      new Runnable() {
                @Override
                public void run() {  image.close(); isProcessingFrame = false;  }
            };

            processImage();

        } catch (final Exception e) {    }
    }

    //Control de la cantidad de Bytes de una imagen
    protected void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    //Procesar la imagen
    private void processImage() {
        imageConverter.run();

        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        rgbFrameBitmap.setPixels(rgbBytes, 0, previewWidth, 0, 0, previewWidth, previewHeight);

        try {
            ModelBandera model = ModelBandera.newInstance(getApplicationContext());
            TensorImage image = TensorImage.fromBitmap(rgbFrameBitmap);

            ModelBandera.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();
            Collections.sort(probability, new CategoryComparator());
            String res="";
            for (int i = 0; i < probability.size(); i++)
                res = res + probability.get(i).getLabel() +  " " +  probability.get(i).getScore()*100 + " % \n";

            txtResults.setText(res);

            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(this);
            String url ="http://www.geognos.com/api/en/countries/info/"+probability.get(0).getLabel()+".json";

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            ArrayList<Pais> paises = new ArrayList<Pais> ();
                            try {
                                JSONObject JSONOBJECTpais = new JSONObject(response);
                                JSONObject JSONOBJECTpais2 = JSONOBJECTpais.getJSONObject("Results");
                                Iterator< String > codigosPaises = JSONOBJECTpais2.keys();
                                while (codigosPaises.hasNext())
                                    paises.add(new Pais(JSONOBJECTpais2.getJSONObject(codigosPaises.next())));

                                PaisAdapter adapatorPais = new PaisAdapter(getApplicationContext(), paises);
                                paisrecycler.setAdapter(adapatorPais);
                            } catch (JSONException e) {
                                //ErrorLog.info(e.getMessage());
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    ErrorLog.error(error.getMessage());
                    Toast.makeText(getApplicationContext(),  error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            // Add the request to the RequestQueue.
            queue.add(stringRequest);


            model.close();
        } catch (IOException e) {
            txtResults.setText("Error al procesar Modelo");
        }

        postInferenceCallback.run();
    }


}

class CategoryComparator implements java.util.Comparator<Category> {
    @Override
    public int compare(Category a, Category b) {
        return (int)(b.getScore()*100) - (int)(a.getScore()*100);
    }
}