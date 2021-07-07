package hwr.demo.tesseract.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import hwr.demo.tesseract.android.sample.CropActivity;

public class MainActivity extends AppCompatActivity {
    TessBaseAPI tessBaseAPI;
    private static final String CAPTURE_PATH = "/OCR_doc";
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;
    private String absolutePath;
    private Uri mImageCaptureUri;
    Button button;
    ImageView imageView;

    TextView textView;
    String dataPath = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, CropActivity.class);
        startActivity(intent);
        imageView = findViewById(R.id.imageView);

        textView = findViewById(R.id.textView);

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPicClick();
                //click();
                //capture();
            }
        });
        dataPath = getFilesDir()+ "/tesseract/";
        checkFile(new File(dataPath + "tessdata/"), "kor");
        checkFile(new File(dataPath + "tessdata/"), "eng");

        // 문자 인식을 수행할 tess 객체 생성
        String lang = "kor+eng";
        tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(dataPath, lang);

    }

    boolean checkLanguageFile(String dir, String lang)
    {
        File file = new File(dir);
        if(!file.exists() && file.mkdirs())
            createFiles(dir);
        else if(file.exists()){
            String filePath = dir + "/" +lang+ ".traineddata";
            File langDataFile = new File(filePath);
            if(!langDataFile.exists())
                createFiles(dir);
            Log.d("msg", "error");
        }
        return true;
    }
    // 파일 존재 확인
    private void checkFile(File dir, String lang) {
        //directory does not exist, but we can successfully create it
        if (!dir.exists()&& dir.mkdirs()){
            copyFiles(lang);
        }
        //The directory exists, but there is no data file in it
        if(dir.exists()) {
            String datafilePath = dataPath+ "/tessdata/" + lang + ".traineddata";
            File datafile = new File(datafilePath);
            if (!datafile.exists()) {
                copyFiles(lang);
            }
        }
    }

    // 파일 복제
    private void copyFiles(String lang) {
        try {
            //location we want the file to be at
            String filepath = dataPath + "/tessdata/" + lang + ".traineddata";

            //get access to AssetManager
            AssetManager assetManager = getAssets();

            //open byte streams for reading/writing
            InputStream inStream = assetManager.open("tessdata/" + lang + ".traineddata");
            OutputStream outStream = new FileOutputStream(filepath);

            //copy the file to the location specified by filepath
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, read);
            }
            outStream.flush();
            outStream.close();
            inStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFiles(String dir)
    {
        AssetManager assetMgr = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = assetMgr.open("tessdata/eng.traineddata");

            String destFile = dir + "/tessdata/eng.traineddata";

            outputStream = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void capture()
    {
//        surfaceView.capture(new Camera.PictureCallback() {
//            @Override
//            public void onPictureTaken(byte[] bytes, Camera camera) {
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inSampleSize = 8;
//
//                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                bitmap = GetRotatedBitmap(bitmap, 90);
//
//                imageView.setImageBitmap(bitmap);
//
//                button.setEnabled(false);
//                button.setText("텍스트 인식중...");
//                new AsyncTess().execute(doGreyscale(bitmap));
//
//                camera.startPreview();
//            }
//        });
    }

    public static Bitmap doGreyscale(Bitmap src)
    {
        final double GS_RED = 0.299;
        final double GS_GREEN = 0.587;
        final double GS_BLUE = 0.114;

        int width = src.getWidth();
        int height = src.getHeight();

        Bitmap resultBitmap = Bitmap.createBitmap(width, height, src.getConfig());
        int A, R, G, B;
        int pixel;

        for (int x = 0; x < width; ++x)
        {
            for (int y = 0; y < height; ++y)
            {
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                R = G = B = (int) (GS_RED * R + GS_GREEN * G + GS_BLUE * B);
                resultBitmap.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        return resultBitmap;
    }

    public synchronized static Bitmap GetRotatedBitmap(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                if (bitmap != b2) {
                    bitmap = b2;
                }
            } catch (OutOfMemoryError ex) {
                ex.printStackTrace();
            }
        }
        return bitmap;
    }

    private class AsyncTess extends AsyncTask<Bitmap, Integer, String> {
        @Override
        protected String doInBackground(Bitmap... mRelativeParams) {
            tessBaseAPI.setImage(mRelativeParams[0]);
            return tessBaseAPI.getUTF8Text();
        }

        protected void onPostExecute(String result) {
            //완료 후 버튼 속성 변경 및 결과 출력
            textView.setText(result);
            Toast.makeText(MainActivity.this, ""+result, Toast.LENGTH_LONG).show();

            button.setEnabled(true);
            button.setText("텍스트 인식");
        }
    }

    public void click(){
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    public void onPicClick() {


        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int which) {

                doTakePhotoAction();

            }

        };

        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int which) {

                doTakeAlbumAction();

            }

        };


        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }

        };

        new AlertDialog.Builder(this,R.style.Theme_AppCompat_Dialog_Alert)
                .setTitle("업로드할 이미지 선택")
                .setPositiveButton("사진촬영", cameraListener)
                .setNeutralButton("취소", cancelListener)
                .setNegativeButton("앨범선택", albumListener)
                .show();
    }



    public void doTakePhotoAction() // 카메라 촬영 후 이미지 가져오기

    {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


        // 임시로 사용할 파일의 경로를 생성

        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";

        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));


        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

        startActivityForResult(intent, PICK_FROM_CAMERA);

    }

    public void doTakeAlbumAction() // 앨범에서 이미지 가져오기

    {

        // 앨범 호출

        Intent intent = new Intent(Intent.ACTION_PICK);

        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);

        startActivityForResult(intent, PICK_FROM_ALBUM);

    }

//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        super.onActivityResult(requestCode,resultCode,data);
//        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
//        SharedPreferences.Editor editor = pref.edit();
//
//        if(resultCode != RESULT_OK)
//
//            return;
//
//
//        switch(requestCode)
//
//        {
//
//            case PICK_FROM_ALBUM:
//
//            {
//
//                // 이후의 처리가 카메라와 같으므로 일단  break없이 진행합니다.
//
//                // 실제 코드에서는 좀더 합리적인 방법을 선택하시기 바랍니다.
//
//                mImageCaptureUri = data.getData();
//
//                Log.d("이미지 경로",mImageCaptureUri.getPath().toString());
//
//            }
//
//
//            case PICK_FROM_CAMERA:
//
//            {
//
//                // 이미지를 가져온 이후의 리사이즈할 이미지 크기를 결정합니다.
//
//                // 이후에 이미지 크롭 어플리케이션을 호출하게 됩니다.
//
//                Intent intent = new Intent("com.android.camera.action.CROP");
//
//                intent.setDataAndType(mImageCaptureUri, "image/*");
//
//
//                // CROP할 이미지를 200*200 크기로 저장
//
//                intent.putExtra("outputX", 200); // CROP한 이미지의 x축 크기
//
//                intent.putExtra("outputY", 200); // CROP한 이미지의 y축 크기
//
//               // intent.putExtra("aspectX", 1); // CROP 박스의 X축 비율
//
//                //intent.putExtra("aspectY", 1); // CROP 박스의 Y축 비율
//
//                intent.putExtra("scale", false);
//
//                intent.putExtra("return-data", true);
//
//                startActivityForResult(intent, CROP_FROM_IMAGE); // CROP_FROM_CAMERA case문 이동
//
//                break;
//
//            }
//
//
//            case CROP_FROM_IMAGE: {
//
//                // 크롭이 된 이후의 이미지를 넘겨 받습니다.
//
//                // 이미지뷰에 이미지를 보여준다거나 부가적인 작업 이후에
//
//                // 임시 파일을 삭제합니다.
//
//                if (resultCode != RESULT_OK) {
//
//                    return;
//
//                }
//
//
//                final Bundle extras = data.getExtras();
//                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
//
//                        +"/" + System.currentTimeMillis()+".jpg";
//
//                if(extras != null)
//
//                {
//
//                    Bitmap bitmap = extras.getParcelable("data"); // CROP된 BITMAP
//
//                    //iv_UserPhoto.setImageBitmap(photo); // 레이아웃의 이미지칸에 CROP된 BITMAP을 보여줌
//
//
//
//                    //storeCropImage(photo, filePath); // CROP된 이미지를 외부저장소, 앨범에 저장한다.
//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inSampleSize = 8;
//
//                    //bitmap = GetRotatedBitmap(bitmap, 90);
//
//                    imageView.setImageBitmap(bitmap);
//
//                    button.setEnabled(false);
//                    button.setText("텍스트 인식중...");
//                    new AsyncTess().execute(doGreyscale(bitmap));
//
//                    //camera.startPreview();
//                    absolutePath = filePath;
//
//
//
//                    break;
//
//
//                }
//
//                // 임시 파일 삭제
//
//                File f = new File(mImageCaptureUri.getPath());
//
//                if(f.exists())
//
//                {
//
//                    f.delete();
//
//                }
//
//            }
//
//
//
//
//
//        }
//
//    }


    private void storeCropImage(Bitmap bitmap, String filePath) {

        // SmartWheel 폴더를 생성하여 이미지를 저장하는 방식이다.

        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + CAPTURE_PATH;

        File directory_SmartWheel = new File(dirPath);


        if(!directory_SmartWheel.exists()) // SmartWheel 디렉터리에 폴더가 없다면 (새로 이미지를 저장할 경우에 속한다.)

            directory_SmartWheel.mkdir();
        File copyFile = new File(filePath);

        BufferedOutputStream out = null;


        try {


            copyFile.createNewFile();

            out = new BufferedOutputStream(new FileOutputStream(copyFile));

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);


            // sendBroadcast를 통해 Crop된 사진을 앨범에 보이도록 갱신한다.

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,

                    Uri.fromFile(copyFile)));

            MediaScannerConnection.scanFile( getApplicationContext(),

                    new String[]{copyFile.getAbsolutePath()},

                    null,

                    new MediaScannerConnection.OnScanCompletedListener(){

                        @Override

                        public void onScanCompleted(String path, Uri uri) {

                            Log.v("File scan", "file:" + path + "was scanned seccessfully");

                        }

                    });



            out.flush();

            out.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        Log.d("file path", filePath);
        Uri imageUri = Uri.fromFile(copyFile);
//        Glide.with(getApplicationContext()).load(imageUri)
//                .centerCrop()
//                //.placeholder(R.drawable.alimi_sample)
//                //.error(R.drawable.alimi_sample)
//                .into(ivImage);


    }
}

