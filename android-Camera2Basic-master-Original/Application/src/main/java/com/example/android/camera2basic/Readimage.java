package com.example.android.camera2basic;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;

public class Readimage extends Activity {
    Button btn_img, btn_decoding = null;
    static TextView txt = null;
    Bitmap bitmap = null;
    ImageView imageView;
    static int flag_data1 = 0;
    static int flag_data2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readimage);

        imageView = (ImageView) findViewById(R.id.imageView);
        File mFile = new File(getExternalFilesDir(null), "pic.jpg");
        Log.d("SAVELOCAL", String.valueOf(mFile));


        if (mFile.exists()) {
            bitmap = BitmapFactory.decodeFile(mFile.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
        }
        btn_img = (Button) findViewById(R.id.btn_img);
        btn_decoding = (Button) findViewById(R.id.btn_decoding);
        txt = (TextView) findViewById(R.id.txt);
        btn_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("video/jpg");
                startActivityForResult(intent, 1);
            }
        });
    }

    //取得相片後返回的監聽式
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //當使用者按下確定後
        if (resultCode == RESULT_OK) {
            //取得圖檔的路徑位置
            Uri uri = data.getData();
            //寫log
            Log.e("uri", uri.toString());
            //抽象資料的接口
            ContentResolver cr = this.getContentResolver();
            try {
                //由抽象資料接口轉換圖檔路徑為Bitmap
                bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                //取得圖片控制項ImageView
                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                // 將Bitmap設定到ImageView
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                Log.e("Exception", e.getMessage(), e);
            }
        }
        btn_decoding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                Log.d("WHWH=", String.valueOf(width) + "," + String.valueOf(height));
                imageView.setImageBitmap(grayscale(bitmap, width, height));
            }
        });

    }

    public static Bitmap grayscale(Bitmap src, int w, int h) {
        // constant factors
        int[][] img_Pixel = new int[w][h];
        float[] gray_value = new float[h];
        final double GS_RED = 0.299;
        final double GS_GREEN = 0.587;
        final double GS_BLUE = 0.114;
        String answer = "";
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        // pixel information
        int A, R, G, B;
        int pixel;
        int c = 0;
        int i = 0, j = 0;
        int sum = 0;
        // get image size
        int width = src.getWidth();//800
        int height = src.getHeight();//600

        // scan through every single pixel
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get one pixel color
                pixel = src.getPixel(x, y);

                // retrieve color of all channels
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                // take conversion up to one single value
                R = G = B = (int) (GS_RED * R + GS_GREEN * G + GS_BLUE * B);
                img_Pixel[x][y] = R;

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        //由左至右相加/寬
        for (int x = 0; x < width * height + 1; x++) {
            if (i < 800) {
                sum += img_Pixel[i][j];
            } else {
                gray_value[c] = (float) sum / 800;
                sum = 0;
                i = 0;
                j++;
                c++;
            }
            i++;
        }
        Log.d("PPIIXXEELL", String.valueOf(gray_value[0]));
        Log.d("PPIIXXEELL", String.valueOf(c));

        //二階多項式擬合
        int N = h;//
        int n = 2, k;
        double[] x = new double[h];
        double[] y = new double[h];
        double[] X = new double[2 * n + 1];
        for (i = 0; i < h; i++) {
            x[i] = i;//0,1,2,3,....600
            y[i] = gray_value[i];
        }
        for (i = 0; i < 2 * n + 1; i++) {
            X[i] = 0;
            for (j = 0; j < N; j++)
                X[i] = X[i] + Math.pow(x[j], i);        //consecutive positions of the array will store N,sigma(xi),sigma(xi^2),sigma(xi^3)....sigma(xi^2n)
        }

        double[][] B1 = new double[n + 1][n + 2];
        double[] a = new double[n + 1];
        for (i = 0; i <= n; i++)
            for (j = 0; j <= n; j++)
                B1[i][j] = X[i + j];
        double[] Y = new double[n + 1];
        for (i = 0; i < n + 1; i++) {
            Y[i] = 0;
            for (j = 0; j < N; j++)
                Y[i] = Y[i] + Math.pow(x[j], i) * y[j];        //consecutive positions will store sigma(yi),sigma(xi*yi),sigma(xi^2*yi)...sigma(xi^n*yi)
        }
        for (i = 0; i <= n; i++)
            B1[i][n + 1] = Y[i];
        n = n + 1;
        /*System.out.print("\nThe Normal(Augmented Matrix) is as follows:\n");
        for (i = 0; i < n; i++)            //print the Normal-augmented matrix
        {
            for (j = 0; j <= n; j++)
                System.out.print(B1[i][j] + "\t");
            System.out.print("\n");
        }*/
        for (i = 0; i < n; i++)                    //From now Gaussian Elimination starts(can be ignored) to solve the set of linear equations (Pivotisation)
            for (k = i + 1; k < n; k++)
                if (B1[i][i] < B1[k][i])
                    for (j = 0; j <= n; j++) {
                        double temp = B1[i][j];
                        B1[i][j] = B1[k][j];
                        B1[k][j] = temp;
                    }

        for (i = 0; i < n - 1; i++)            //loop to perform the gauss elimination
            for (k = i + 1; k < n; k++) {
                double t = B1[k][i] / B1[i][i];
                for (j = 0; j <= n; j++)
                    B1[k][j] = B1[k][j] - t * B1[i][j];    //make the elements below the pivot elements equal to zero or elimnate the variables
            }
        for (i = n - 1; i >= 0; i--)                //back-substitution
        {                        //x is an array whose values correspond to the values of x,y,z..
            a[i] = B1[i][n];                //make the variable to be calculated equal to the rhs of the last equation
            for (j = 0; j < n; j++)
                if (j != i)            //then subtract all the lhs values except the coefficient of the variable whose value                                   is being calculated
                    a[i] = a[i] - B1[i][j] * a[j];
            a[i] = a[i] / B1[i][i];            //now finally divide the rhs by the coefficient of the variable to be calculated
        }
        /*System.out.print("\nThe values of the coefficients are as follows:\n");
        for (i = 0; i < n; i++)
            System.out.print("x^" + i + "=" + a[i] + "\n");            // Print the values of x^0,x^1,x^2,x^3,....
        System.out.print("\nHence the fitted Polynomial is given by:\ny=");
        for (i = 0; i < n; i++)
            Log.d("AI", "+(" + a[i] + ")" + "x^" + i);
        System.out.print("\n");*/

        //算出a係數後，算出polyval------------------

        double[] x2 = new double[h];
        double[] y2 = new double[h];
        for (i = 0; i < h; i++) {
            x2[i] = i;
            //System.out.print(x2[i]+"\n");
        }
        for (i = 0; i < h; i++) {
            y2[i] = a[0] * Math.pow(x2[i], 0) + a[1] * Math.pow(x2[i], 1) + a[2] * Math.pow(x2[i], 2);
            //System.out.print(i+"="+y2[i]+"\n");
        }
        for (i = 0; i < h; i++) {
            if (y2[i] > y[i]) {
                y2[i] = 0;
            } else {
                y2[i] = 255;
            }
        }
//        for(i=500;i<h;i++){
//            Log.d("YYY222",(i+"="+y2[i]));
//        }
        for (i = 0; i < width; i++) {
            for (j = 0; j < height; j++) {
                //bmOut.setPixel(i,j,0xFF00FF00);
                if (y2[j] > 0) {
                    bmOut.setPixel(i, j, 0xFFFFFFFF);
                } else {
                    bmOut.setPixel(i, j, 0xFF000000);
                }
            }
        }
        // return final image

        //邏輯判斷
        int sum_B = 0;
        int sum_W = 0;
        int sum1 = 0;
        int sum2 = 0;
        int cc = 0;
        int count = 1;
        String ch = "";
        for (i = 0; i < h; i++) {
            if (y2[i] == 0) {//暗條紋
                sum_B = sum_B + 1;
                sum_W = 0;
            } else {
                sum_B = 0;
            }
            if (sum_B == 3) {//超過6列判斷為黑條紋
                sum_B = 0;
                ch += '0';
                cc = cc + 1;
                sum1 = sum1 + 1;
            }

            if (y2[i] > 10) {//亮條紋
                sum_W = sum_W + 1;
                sum_B = 0;
            } else {
                sum_W = 0;
            }
            if (sum_W == 4) {//超過6列判斷為亮條紋
                sum_W = 0;
                ch += '1';
                cc = cc + 1;
                sum2 = sum2 + 1;
            }
        }
//        Log.d("COLOR=", String.valueOf(sum1));
//        Log.d("COLOR=", String.valueOf(sum2));
//        Log.d("COLOR=", ch);
        String str_final = "";
        String display_str = "";
        for (i = 0; i < ch.length(); i++) {
            if (i < sum1 + sum2 - 20) {
                //Log.d("header_Ch=", (String) ch.subSequence(i, i + 10));
                String str_header = "1101010100";
                if (ch.subSequence(i, i + 10).equals(str_header)) {
                    //Log.d("header_Ch_str=", i+"~"+i+10 );
                    String str_data = (String) ch.subSequence(i + 10, i + 20);
                    Log.d("header_str_data=", str_data);
                    for (j = 0; j < 10; j++) {
                        if ((j % 10) != 0 && (j % 10) != 9) {
//                            Log.d("str_j=", String.valueOf(j));
//                            Log.d("str_data=", String.valueOf(str_data.substring(j, j + 1)));
                            str_final += str_data.substring(j, j + 1);
                        }
                    }

                    Log.d("str_final=", str_final);//最終8bit字串
                    StringBuffer sb = new StringBuffer();
                    sb.append(str_final);
                    sb.reverse();
                    Log.d("reverse_str_final=", String.valueOf(sb));//反轉最終8bit字串
                    String answer_dec = Integer.valueOf(String.valueOf(sb), 2).toString();
                    int answer_hex = 255 - Integer.parseInt(answer_dec);//字串轉數子(1111-1111)=255取反相
                    answer = Integer.toHexString(answer_hex);
                    display_str += answer;
//                    display_str += answer;
                    Log.d("answer=", answer);//反轉最終8bit字串
                    txt.setText(display_str);
                    if (answer.equals("cc") && flag_data1 == 0) {
                        flag_data1++;
                    }
                    if (answer.equals("33") && flag_data2 == 0) {
                        flag_data2++;
                    }
                    if (flag_data1 == 1 && flag_data1 == 1) {
                        flag_data1=flag_data2=0;
                        Log.d("NEED", "jump");
                    }
                    str_final = "";//清除
                }
            } else {
                Log.d("Not_find_heade=", "Not find heade");
                break;
            }
        }
        return bmOut;

    }
}
