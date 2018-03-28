package com.example.anuj.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebName = new ArrayList<>();
    ArrayList<String> celebUrl = new ArrayList<>();

    int count;
    int score;
    int total;
    int correctOption;
    boolean active;

    CountDownTimer countDownTimer;

    Random random=new Random();

    public class HtmlDownload extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {

            try {
                URL url=new URL(strings[0]);
                HttpURLConnection connection= (HttpURLConnection) url.openConnection();
                InputStream in=connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder html = new StringBuilder();
                String line=null;

                while((line = reader.readLine()) != null ) {
                    html.append(line);
                }

                in.close();
                Log.i("dfg",html.toString());
                return html.toString();
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void detailDownload(){

        HtmlDownload ob=new HtmlDownload();
        String add="";

        try {
            add=ob.execute("http://www.bollywoodhungama.com/celebrities/top-100/").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Pattern name=Pattern.compile("role=\"img\" title=\"(.*?)\" srcset=");
        Matcher mname=name.matcher(add);
        Pattern src=Pattern.compile("srcset=\"(.*?) ");
        Matcher msrc=src.matcher(add);

        for(int i=1;i<=10;i++)
        {
            mname.find();
            msrc.find();
        }

        while(mname.find() && msrc.find())
        {
            if (mname.group(1).equals("Ileana D&#8217;Cruz"))
                celebName.add("Ileana D Cruz");
            else
                celebName.add(mname.group(1));

            celebUrl.add(msrc.group(1));
            mname.find();
            msrc.find();
        }

    }

    public class DownloadImage extends AsyncTask<String,Void,Bitmap>{

        @Override
        protected Bitmap doInBackground(String... images) {

            try {
                URL url=new URL(images[0]);
                HttpURLConnection con= (HttpURLConnection) url.openConnection();
                con.connect();
                InputStream in=con.getInputStream();
                Bitmap img= BitmapFactory.decodeStream(in);
                return img;
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

    }

    public void createTimer(long time){

        if (time>10000)
            time=10000;

        final TextView timer=findViewById(R.id.timer);

        if (countDownTimer!=null)
            countDownTimer.cancel();

        countDownTimer=new CountDownTimer(time + 100,1000) {

            @Override
            public void onTick(long l) {
                if (l<10000)
                    timer.setText("0" + String.valueOf(l / 1000) + "s");
                else
                    timer.setText("10s");
            }

            @Override
            public void onFinish() {
                timer.setText("00s");
                active = false;

                final TextView timesUp = findViewById(R.id.timesUp);
                timesUp.setTranslationY(1000f);
                timesUp.setVisibility(View.VISIBLE);
                timesUp.animate().translationYBy(-1000f).setDuration(2000);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        timesUp.setVisibility(View.INVISIBLE);
                        LinearLayout playLayout=findViewById(R.id.playLayout);
                        playLayout.setVisibility(View.INVISIBLE);
                        TextView timer=findViewById(R.id.timer);
                        timer.setVisibility(View.INVISIBLE);

                        GridLayout playMoreLayout = findViewById(R.id.playMoreLayout);
                        String scoreText = "Score\n"+score+"/"+total;
                        TextView scoreView = (TextView) playMoreLayout.getChildAt(1);
                        scoreView.setText(scoreText);
                        playMoreLayout.setVisibility(View.VISIBLE);
                    }
                },3000);

            }
        }.start();
    }

    public void setQuestion(int pos){

        total++;
        ImageView imageView = findViewById(R.id.imageView);
        DownloadImage downloadImage = new DownloadImage();

        try {
            Bitmap image = downloadImage.execute(celebUrl.get(pos)).get();
            imageView.setImageBitmap(image);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        LinearLayout optionLayout = findViewById(R.id.optionLayout);

        int opSeq[][]={{1,2,3,4},{2,3,4,1},{3,4,1,2},{4,1,2,3},{1,4,2,3},{4,2,1,3},{3,1,2,4},{2,3,1,4},
                {1,2,4,3},{1,3,2,4},{1,3,4,2},{1,4,3,2},{2,1,3,4},{2,1,4,3},{2,4,1,3},{2,4,3,1},
                {3,1,4,2},{3,2,1,4},{3,2,4,1},{3,4,2,1},{4,1,3,2},{4,2,3,1},{4,3,1,2},{4,3,2,1}};

        int optionSeq = random.nextInt(24);
        correctOption = opSeq[optionSeq][0];
        int order=0;

        for(int i : opSeq[optionSeq])
        {
            ((TextView)(optionLayout.getChildAt(i-1))).setText(celebName.get((pos+order)%100));

            while((order=random.nextInt(100)) == 0);
        }

    }

    public void check(View view){

        if (active) {

            long extraTime=-500;
            TextView textView = (TextView) view;

            int answerGiven = Integer.parseInt(textView.getTag().toString());

            final Toast toast = Toast.makeText(MainActivity.this, "Wrong!!", Toast.LENGTH_SHORT);

            if (answerGiven == correctOption) {
                score++;
                toast.setText("Correct!!");
                extraTime=1500;
            }

            toast.show();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 1000);

            TextView timer = findViewById(R.id.timer);
            String time = timer.getText().toString();
            time = time.substring(0,2);

            setQuestion(count++);

            createTimer(((Long.parseLong(time)*1000) + extraTime));
        }
    }

    public void playMore(View view){

        TextView play = (TextView) view;
        Button button = findViewById(R.id.goButton);
        score = 0;
        total = 0;

        int userChoice = Integer.parseInt(play.getTag().toString());

        if (userChoice == 1)
        {
            count--;
            start(button);
        }
        else
        {
            finish();
            System.exit(0);
        }
    }


    public void start(View view){

        active=true;

        Button button=(Button)view;
        button.setVisibility(View.INVISIBLE);
        GridLayout playMoreLayout = findViewById(R.id.playMoreLayout);
        playMoreLayout.setVisibility(View.INVISIBLE);
        LinearLayout playLayout=findViewById(R.id.playLayout);
        playLayout.setVisibility(View.VISIBLE);
        TextView timer=findViewById(R.id.timer);
        timer.setVisibility(View.VISIBLE);

        setQuestion((count++)%100);
        createTimer(10000);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        count = 0;
        score = 0;
        detailDownload();
    }
}
