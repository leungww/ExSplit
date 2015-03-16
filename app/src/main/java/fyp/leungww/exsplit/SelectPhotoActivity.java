package fyp.leungww.exsplit;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.leptonica.android.Binarize;
import com.googlecode.leptonica.android.Convert;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.Rotate;
import com.googlecode.leptonica.android.Skew;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SelectPhotoActivity extends ActionBarActivity {
    public static final int CAMERA_REQUEST_CODE=1;
    public static final int GALLERY_REQUEST_CODE=2;
    public static final int CROP_PHOTO_REQUEST_CODE=3;

    public static final String ITEMS_PARCELABLE="items_parcelable";
    public static final String DIRECTORY_PATH = Environment.getExternalStorageDirectory().toString() + "/ExSplit/";
    public static final String TESSDATA_PATH = DIRECTORY_PATH+"tessdata/";
    public static final String LANGUAGE_ENGLISH="eng";

    private final String language = "eng";
    private Button selectphoto_camera, selectphoto_gallery, selectphoto_add;
    private EditText selectphoto_recognised_text;
    private LinearLayout selectphoto_item_list;
    private String photoPath;
    private File cameraFile;

    private String currencyCodeSymbol;
    private List<Long> travellers_id;
    private List<String> travellers_name;

    private List<String> items_name_eventSplit = new ArrayList<>();
    private List<Double> items_price_evenSplit = new ArrayList<>();
    private List<String> items_name_byAmount = new ArrayList<>();
    private List<Double> items_price_byAmount = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_photo);
        Toolbar toolbar= (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();
        BillParcelable billParcelable = intent.getParcelableExtra(AddANewBillStep1Fragment.BILL_PARCELABLE);
        currencyCodeSymbol = billParcelable.getCurrencyCodeSymbol();
        travellers_id = billParcelable.getTravellers_id();
        travellers_name = billParcelable.getTravellers_name();

        selectphoto_camera = (Button) findViewById(R.id.selectphoto_camera);
        selectphoto_camera.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    try {
                        cameraFile = createPhotoFile();
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile));
                        intent.putExtra("scale", true);
                        intent.putExtra("return-data", false);
                        intent.putExtra("noFaceDetection", true);
                        startActivityForResult(intent, CAMERA_REQUEST_CODE);
                    } catch (IOException e) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Error occured while creating image file", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
        });

        selectphoto_gallery = (Button) findViewById(R.id.selectphoto_gallery);
        selectphoto_gallery.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, GALLERY_REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Cannot locate gallery", Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        });

        selectphoto_item_list = (LinearLayout) findViewById(R.id.selectphoto_item_list);
        selectphoto_recognised_text = (EditText) findViewById(R.id.selectphoto_recognised_text);
        selectphoto_recognised_text.setHint(getString(R.string.item_name)+"  "+getString(R.string.price)+" ("+currencyCodeSymbol+")");
        selectphoto_recognised_text.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                extractItems(s.toString());
            }
        });
        selectphoto_add = (Button) findViewById(R.id.selectphoto_add);
        selectphoto_add.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                addItems();
            }
        });
    }

    private void addItems(){
        if((items_name_eventSplit.isEmpty() || items_price_evenSplit.isEmpty()) && (items_name_byAmount.isEmpty() || items_price_byAmount.isEmpty())){
            Toast toast = Toast.makeText(this, "No item to add", Toast.LENGTH_SHORT);
            toast.show();
        }else{
            List<List<Double>> items_amounts = new ArrayList<>();
            List<String> items_amounts_string = new ArrayList<>();
            BigDecimal sizeBD = BigDecimal.valueOf(travellers_id.size());
            for(int index=0;index<items_price_evenSplit.size();index++){
                List<Double> amounts = new ArrayList<>();
                BigDecimal priceBD = BigDecimal.valueOf(items_price_evenSplit.get(index));
                BigDecimal evenAmountBD = priceBD.divide(sizeBD, AddItemActivity.EVEN_SPLIT_ROUNDING_DP, BigDecimal.ROUND_HALF_UP);
                double evenAmount = evenAmountBD.doubleValue();
                BigDecimal lastAmountBD = priceBD.subtract(evenAmountBD.multiply(sizeBD.subtract(BigDecimal.ONE)));
                double lastAmount = lastAmountBD.doubleValue();
                for(int i=0;i<travellers_id.size()-1;i++){
                    amounts.add(evenAmount);
                }
                //Last traveller takes the rounding remains
                amounts.add(lastAmount);
                List<String> names_amounts = new ArrayList<>();
                for(int i=0;i<travellers_name.size()-1;i++){
                    names_amounts.add(travellers_name.get(i)+" ("+evenAmount+")");
                }
                names_amounts.add(travellers_name.get(travellers_name.size()-1)+" ("+lastAmount+")");
                String amounts_string = TextUtils.join(", ", names_amounts);
                items_amounts.add(amounts);
                items_amounts_string.add(amounts_string);
            }
            ItemsParcelable itemsParcelable = new ItemsParcelable(travellers_id, items_name_eventSplit, items_price_evenSplit, items_amounts, items_amounts_string,items_name_byAmount,items_price_byAmount);
            Intent resultIntent = new Intent();
            resultIntent.putExtra(ITEMS_PARCELABLE,itemsParcelable);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (CAMERA_REQUEST_CODE):
                if (resultCode == RESULT_OK) {
                    Intent intent = getCropIntent();
                    intent.setDataAndType(Uri.fromFile(cameraFile), "image/*");
                    startActivityForResult(intent, CROP_PHOTO_REQUEST_CODE);
                }else if(resultCode == RESULT_CANCELED){
                    cameraFile.delete();
                }
                break;
            case (GALLERY_REQUEST_CODE):
                if (resultCode == RESULT_OK) {
                    Intent intent = getCropIntent();
                    Uri selectedImage = data.getData();
                    intent.setDataAndType(selectedImage, "image/*");
                    startActivityForResult(intent, CROP_PHOTO_REQUEST_CODE);
                    /*if (data == null) {
                        Log.i("SelectPhoto", "Null data, but RESULT_OK, from image picker!");
                        Toast.makeText(this, "No photo was selected", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap receipt = extras.getParcelable("data");
                        Log.i("SelectPhoto","done");*/
                        /*File tempFile = getTempFile();
                        // new logic to get the photo from a URI
                        if (data.getAction() != null) {
                            processPhotoUpdate(tempFile);
                        }*/
                }
                break;
            case CROP_PHOTO_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(getPhotoUri());
                    this.sendBroadcast(intent);

                    //final Bundle extras = data.getExtras();
                    //Bitmap receipt = extras.getParcelable("data");
                    OCRPhoto ocr = new OCRPhoto();
                    ocr.execute(photoPath);
                }
                break;
        }
    }

    private Intent getCropIntent(){
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0);
        intent.putExtra("aspectY", 0);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoUri());
        //intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);

        return intent;
    }

    private File createPhotoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "Receipt_" + timeStamp;
        String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/Camera/";
        File directory = new File(directoryPath);
        return File.createTempFile(fileName, ".jpg", directory);
    }

    private Uri getPhotoUri(){
        photoPath = DIRECTORY_PATH + "/receipt_cropped.jpg";
        File file = new File(DIRECTORY_PATH + "/receipt_cropped.jpg");
        return Uri.fromFile(file);
    }

    private void extractItems(String text){
        if ( language.equalsIgnoreCase(LANGUAGE_ENGLISH) ) {
            text = text.replaceAll("[£$]+", " ");
        }
        String lines[] = text.split("\n");
        Pattern pattern = Pattern.compile("([+-]?[0-9]*[\\s]*[\\.\\,]?[0-9\\s]+$)");
        LayoutInflater inflater = getLayoutInflater();
        selectphoto_item_list.removeAllViews();
        items_name_eventSplit = new ArrayList<>();
        items_price_evenSplit = new ArrayList<>();
        items_name_byAmount = new ArrayList<>();
        items_price_byAmount = new ArrayList<>();
        List<View> item_rows_evenSplit = new ArrayList<>();
        List<View> item_rows_byAmount = new ArrayList<>();
        for(String line:lines){
            Matcher matcher = pattern.matcher(line);
            int beginIndex = 0;
            while (matcher.find()) {
                String name = line.substring(beginIndex, matcher.start()).trim();
                beginIndex = matcher.end();
                String price_string = matcher.group().trim().replace(",",".").replaceAll("\\s+", "");
                try {
                    double price = Double.parseDouble(price_string);
                    BigDecimal priceBD = BigDecimal.valueOf(price);
                    BigDecimal sizeBD = BigDecimal.valueOf(travellers_id.size());
                    BigDecimal evenAmountBD = priceBD.divide(sizeBD, AddItemActivity.EVEN_SPLIT_ROUNDING_DP, BigDecimal.ROUND_HALF_UP);
                    BigDecimal lastAmountBD = priceBD.subtract(evenAmountBD.multiply(sizeBD.subtract(BigDecimal.ONE)));
                    if(name.length() > 0 && price != 0){
                        View item_row_partial = inflater.inflate(R.layout.item_row_partial, null);
                        TextView item_name = (TextView) item_row_partial.findViewById(R.id.item_name);
                        item_name.setText(name);
                        TextView item_currency = (TextView) item_row_partial.findViewById(R.id.item_currency);
                        item_currency.setText(currencyCodeSymbol);
                        TextView item_price = (TextView) item_row_partial.findViewById(R.id.item_price);
                        item_price.setText(price+"");
                        if((price > 0 && evenAmountBD.doubleValue() > 0 && lastAmountBD.doubleValue() > 0) ||
                                (price < 0 && evenAmountBD.doubleValue() < 0 && lastAmountBD.doubleValue() < 0)){
                            items_name_eventSplit.add(name);
                            items_price_evenSplit.add(price);
                            item_rows_evenSplit.add(item_row_partial);
                        }else{
                            item_name.setTextColor(getResources().getColor(R.color.primaryColorLight));
                            items_name_byAmount.add(name);
                            items_price_byAmount.add(price);
                            item_rows_byAmount.add(item_row_partial);
                        }
                    }
                }catch(NumberFormatException e){

                }
            }
        }
        if(item_rows_evenSplit.size()>0){
            TextView note_invalid_items = new TextView(SelectPhotoActivity.this);
            note_invalid_items.setTextColor(getResources().getColor(R.color.primaryColorDark));
            note_invalid_items.setText(getString(R.string.note_items_even_split));
            selectphoto_item_list.addView(note_invalid_items);
            for(View item_row:item_rows_evenSplit){
                selectphoto_item_list.addView(item_row);
            }
        }
        if(item_rows_byAmount.size()>0){
            TextView note_invalid_items = new TextView(SelectPhotoActivity.this);
            note_invalid_items.setTextColor(getResources().getColor(R.color.primaryColorLight));
            note_invalid_items.setText(getString(R.string.note_items_by_amount));
            selectphoto_item_list.addView(note_invalid_items);
            for(View item_row:item_rows_byAmount){
                selectphoto_item_list.addView(item_row);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class OCRPhoto extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... params) {
            //Move trained data for tess-two from assets folder to TESSTWO_PATH
            List<String> paths = new ArrayList<String>(){
                {
                    add(DIRECTORY_PATH);
                    add(TESSDATA_PATH);
                }
            };

            for (String path : paths) {
                File dir = new File(path);
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        return "Directory "+path+" cannot be created";
                    }
                }
            }

            if (!(new File(TESSDATA_PATH+language+".traineddata")).exists()) {
                try {
                    AssetManager assetManager = getAssets();
                    InputStream inputStream = assetManager.open("tessdata/"+language+".traineddata");
                    OutputStream outputStream = new FileOutputStream(TESSDATA_PATH+language+".traineddata");

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    inputStream.close();
                    outputStream.close();
                } catch (IOException e) {
                    return language+" trained data cannot be retrieved";
                }
            }

            String photoPath = params[0];

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);

            //Rotate photo
            try {
                ExifInterface exif = new ExifInterface(photoPath);
                int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
                int rotation = 0;

                switch (exifOrientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotation = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotation = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotation = 270;
                        break;
                }

                if (rotation != 0) {
                    int photoWidth = bitmap.getWidth();
                    int photoHeight = bitmap.getHeight();
                    Matrix matrix = new Matrix();
                    matrix.preRotate(rotation);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, photoWidth, photoHeight, matrix, false);
                }

                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            } catch (IOException e) {
                return "Photo cannot be rotated";
            }

            //Convert photo to gray scale
            Pix pix = ReadFile.readBitmap(bitmap);
            Convert.convertTo8(pix);

            //Binarize photo using otsu's algorithm
            Binarize.otsuAdaptiveThreshold(pix);

            //AdaptiveMap.backgroundNormMorph(pix);
            //Enhance.unsharpMasking(pix, 2, 0.5f);

            //Rotate to reduce skewness
            float skew = Skew.findSkew(pix);
            Rotate.rotate(pix, skew, true);

            //File photoFile = new File(DIRECTORY_PATH + "/receipt_processed.jpg");
            //File photoFile = new File(DIRECTORY_PATH + "/receipt_cropped.jpg");
            //WriteFile.writeImpliedFormat(pix, photoFile);

            /*FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(photoPath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                return "Processed photo cannot be saved";
            }*/

            TessBaseAPI tessBaseAPI = new TessBaseAPI();
            tessBaseAPI.setDebug(true);
            tessBaseAPI.init(DIRECTORY_PATH, language);
            //tessBaseAPI.setImage(pix);
            if ( language.equalsIgnoreCase(LANGUAGE_ENGLISH) ) {
                tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890abcdefghijklmnopqrstuvwxyz"
                        +"\\.\\,-+\\*\\s@#%&()[]?{}<>=_£$/\\");
            }
            tessBaseAPI.setImage(bitmap);
            String ocrText = tessBaseAPI.getUTF8Text();
            tessBaseAPI.end();

            ocrText = ocrText.trim();
            return ocrText;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            selectphoto_recognised_text.setText(result);
            extractItems(result);
        }
    }
}

class ItemsParcelable implements Parcelable {
    private List<Long> travellers_id;
    private List<String> items_name_evenSplit;
    private List<Double> items_price_evenSplit;
    private List<List<Double>> items_amounts_evenSplit;
    private List<String> items_amounts_string_evenSplit;
    private List<String> items_name_byAmount;
    private List<Double> items_price_byAmount;

    public ItemsParcelable(List<Long> travellers_id, List<String> items_name_evenSplit, List<Double> items_price_evenSplit,
                           List<List<Double>> items_amounts_evenSplit, List<String> items_amounts_string_evenSplit, List<String> items_name_byAmount, List<Double> items_price_byAmount){
        this.travellers_id = travellers_id;
        this.items_name_evenSplit = items_name_evenSplit;
        this.items_price_evenSplit = items_price_evenSplit;
        this.items_amounts_evenSplit  = items_amounts_evenSplit;
        this.items_amounts_string_evenSplit = items_amounts_string_evenSplit;
        this.items_name_byAmount = items_name_byAmount;
        this.items_price_byAmount = items_price_byAmount;
    }

    public List<Long> getTravellers_id(){
        return travellers_id;
    }

    public List<String> getItems_name_evenSplit(){
        return items_name_evenSplit;
    }

    public List<Double> getItems_price_evenSplit(){
        return items_price_evenSplit;
    }

    public List<List<Double>> getItems_amounts_evenSplit(){
        return items_amounts_evenSplit;
    }

    public List<String> getItems_amounts_string_evenSplit(){
        return items_amounts_string_evenSplit;
    }

    public List<String> getItems_name_byAmount(){
        return items_name_byAmount;
    }

    public List<Double> getItems_price_byAmount(){
        return items_price_byAmount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeList(travellers_id);
        out.writeList(items_name_evenSplit);
        out.writeList(items_price_evenSplit);
        out.writeInt(items_amounts_evenSplit.size());
        for (List<Double> item_amounts: items_amounts_evenSplit) {
            out.writeList(item_amounts);
        }
        out.writeList(items_amounts_string_evenSplit);
        out.writeList(items_name_byAmount);
        out.writeList(items_price_byAmount);
    }

    public static final Parcelable.Creator<ItemsParcelable> CREATOR = new Parcelable.Creator<ItemsParcelable>() {
        public ItemsParcelable createFromParcel(Parcel in) {
            return new ItemsParcelable(in);
        }

        public ItemsParcelable[] newArray(int size) {
            return new ItemsParcelable[size];
        }
    };

    private ItemsParcelable(Parcel in) {
        travellers_id = new ArrayList<>();
        travellers_id = in.readArrayList(Long.class.getClassLoader());
        items_name_evenSplit = new ArrayList<>();
        items_name_evenSplit = in.readArrayList(String.class.getClassLoader());
        items_price_evenSplit = new ArrayList<>();
        items_price_evenSplit = in.readArrayList(Double.class.getClassLoader());
        int size = in.readInt();
        items_amounts_evenSplit = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            List<Double> item_amounts = new ArrayList<>();
            item_amounts = in.readArrayList(Double.class.getClassLoader());
            items_amounts_evenSplit.add(item_amounts);
        }
        items_amounts_string_evenSplit = new ArrayList<>();
        items_amounts_string_evenSplit = in.readArrayList(String.class.getClassLoader());
        items_name_byAmount = new ArrayList<>();
        items_name_byAmount = in.readArrayList(String.class.getClassLoader());
        items_price_byAmount = new ArrayList<>();
        items_price_byAmount = in.readArrayList(Double.class.getClassLoader());
    }
}
