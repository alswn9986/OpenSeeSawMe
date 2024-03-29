package com.example.openseesawme;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MyAdapter1 extends BaseAdapter {
    Context context;
    int layout;
    LayoutInflater inf;

    int img;
    String result="";

    String[] gData0;    //인덱스
    String[] gData1;    //출입가능 날짜
    String[] gData2;    //출입가능 요일
    String[] gData3;    //게트 이름
    String[] gData4;    //게스트키 준 날짜
    String[] gData5;    //게스트키 사용 여부
    String[] gData6;    //게스트키 수락 여부
    String[] gData7;  //게스트키 준 사람 이름
    String[] gData8;    //게스트 이미지



    public MyAdapter1(Context context, int layout, String result, String[]... gData) {
        this.context = context;
        this.layout = layout;
        this.img = R.drawable.person1;
        this.result = result;
        this.gData0 = gData[0];
        this.gData1 = gData[1];
        this.gData2 = gData[2];///
        this.gData3 = gData[3];
        this.gData4 = gData[4];
        this.gData5 = gData[5];
        this.gData6 = gData[6];
        this.gData7 = gData[7];
        this.gData8 = gData[8];

        inf = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);


    }
    @Override
    public int getCount() {return gData0.length;}

    @Override
    public Object getItem(int position) {return 0;}//img[position]

    @Override
    public long getItemId(int position) {return position;}

    //getView() =======================================
    public View getView(final int position, View convertView, ViewGroup vg) {
        if (convertView==null)
            convertView = inf.inflate(layout, null);

        //guest.xml 변수들
        ImageView img_guest = (ImageView)convertView.findViewById(R.id.img_guest);
        TextView txt_gname = convertView.findViewById(R.id.txt_gname);
        TextView txt_valdate = convertView.findViewById(R.id.txt_valdate);
        TextView txt_keyfrom = convertView.findViewById(R.id.txt_keyfrom);
        TextView txt_dday = convertView.findViewById(R.id.txt_dday);
        LinearLayout linear_black = convertView.findViewById(R.id.linear_black);
        LinearLayout sentgkey = convertView.findViewById(R.id.sentgkey);

//        iv.setImageResource(img[position]);

        //게스트 이미지
        if(gData8[position].equals("basicUser")){
            img_guest.setImageResource(R.drawable.person1);
        }
        else{
            //변수 : gData6[position]
            Picasso.with(context)
                    .load("http://128.134.114.250:8080/doorlock/uImages/"+gData8[position])
                    .placeholder(R.drawable.person1)//이미지가 존재하지 않을 경우   경우 대체 이미지
                    /*.resize(2000, 2000) // 이미지 크기를 재조정하고 싶을 경우*/
                    .into(img_guest);
        }
        img_guest.setBackground(new ShapeDrawable(new OvalShape()));
        if(Build.VERSION.SDK_INT >= 21) {
            img_guest.setClipToOutline(true);
        }

        //게스트 이름
        txt_gname.setText(gData3[position]);

        final String yoil = gData2[position].substring(0,gData2[position].length()-1);

        //출입날짜 or 출입 요일
        if("null".equals(gData1[position])){
            txt_valdate.setText("출입 가능 요일 : " + yoil);
            Log.i("myguestkeydfffffff", "날짜 null :  ");
        }else if("null".equals(gData2[position])){
            txt_valdate.setText("출입 날짜 : " + gData1[position]);
            Log.i("myguestkeydfffffff", "요일 null :  ");
        }
        //게스트키 준 사람 id
        txt_keyfrom.setText("From."+ gData7[position]);

        //사용한 게스트키는 어둡게(?)
        Log.i("gData4[position]", gData5[position]);
        if(gData5[position].equals("a")){
            linear_black.setVisibility(View.INVISIBLE);
        }else if(gData5[position].equals("b")){
            linear_black.setVisibility(View.VISIBLE);
        }


        //d-day 구하기  (parse 쓸때는 try문에)///이게 안된다.......try문 다시 쓰기
        try {
            java.util.Calendar cal = java.util.Calendar.getInstance(); //일단 Calendar 객체
            String[] mdday;
            if("null".equals(gData1[position])){
                txt_dday.setText(yoil);
            }else{
                mdday =  gData1[position].split("-");
                int year = Integer.parseInt(mdday[0]);
                int month = Integer.parseInt(mdday[1]);
                int date = Integer.parseInt(mdday[2]);


                long now_day = cal.getTimeInMillis(); //현재 시간

                cal.set(year, month-1, date); //목표일을 cal에 set

                long event_day = cal.getTimeInMillis(); //목표일에 대한 시간
                long d_day = (event_day - now_day) / (60*60*24*1000);

                Log.i("dday는????", Long.toString(d_day));

                txt_dday.setText(d_day + "일 남음");
                if(d_day == 0){
                    txt_dday.setText("D-day");
                }
                else if( d_day > 0){
                    txt_dday.setText(d_day + "일 남음");
                }else if( d_day > 0 && gData5[position].equals("b")){
                    txt_dday.setText("출입 완료");
                } else if( d_day < 0){
                    txt_dday.setText("사용 완료");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //사용한 게스트키는 어둡게(?)
        Log.i("gData4[position]", gData4[position]);
        if(gData4[position].equals("a")){
            linear_black.setVisibility(View.INVISIBLE);
        }else if(gData4[position].equals("b")){
            linear_black.setVisibility(View.VISIBLE);
        }

        //해당 아이템? 선택시 OtherMemo2.java로 넘기는 이벤트
        sentgkey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OtherMemo2.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("gData0", gData0[position]);
                intent.putExtra("gData1", gData1[position]);
                intent.putExtra("gData2", yoil);
                intent.putExtra("gData3", gData3[position]);
                intent.putExtra("gData4", gData4[position]);
                intent.putExtra("gData5", gData5[position]);
                intent.putExtra("gData6", gData6[position]);
                intent.putExtra("gData7", gData7[position]);
                context.startActivity(intent);
            }
        });



        return convertView;
    }//getView() end
}

