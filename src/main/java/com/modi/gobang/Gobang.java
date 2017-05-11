package com.modi.gobang;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/8.
 */

public class Gobang extends View {
	//默认5*5规格
	private final static int DEFSTANDARD = 8;
	private final static int DEFBACKGROUD = 0xff0;
	private final static int DEFLINECOLOR = 0x000;
	//默认3px
	private final static float DEFLINEWIDTH = 1;
	private final static float DEFDOTRADIUS = 5;
	private final static int DEFDOTCOLOR = 0x000;
	private final static int DEFCHESSMANCOLOR1 = 0x000;
	private final static int DEFCHESSMANRADIUS1 = 10;
	private final static int DEFCHESSMANCOLOR2 = 0xfff;
	private final static int DEFCHESSMANRADIUS2 = 10;

	private int cPivotX = 0;
	private int cPivotY = 0;

	//记录中心点
	private List<Map<String,Integer>> dots = null;
	//记录点击后需要绘制的棋子
	// private Map<String,Integer> mustDots = null;
	private List<Map<String,Integer>> mustDots = null;
	//记录所以坐标点：X,Y相交的点
	private List<Map<String,Integer>> totalDots = null;

	//用来切换绘制不同棋子
	private boolean isChange = false;

	private enum ORIENTATION {
		HORIZONTAL,VERTICAL
	};
	// 定义画笔
	private Paint mPaint;
	/*	<!--规格参数：表示是X * X的正方形格子：如4*4-->
        <attr name="standard" format="integer"/>
        <attr name="backgroud" format="color"/>
        <!--网格线的颜色及宽度-->
        <attr name="line_color" format="color"/>
        <attr name="line_width" format="dimension"/>
        <!--中心点的颜色及半径-->
        <attr name="dot_radius" format="float"/>
        <attr name="dot_color" format="color"/>
        <!--棋子颜色及半径-->
        <attr name="chessman_color1" format="color"/>
        <attr name="chessman_radius1" format="color"/>
        <attr name="chessman_color2" format="color"/>
        <attr name="chessman_radius2" format="color"/>*/
	private int standard;
	private int backgroud;
	private int lineColor;
	private float lineWidth;
	private float dotRadius;
	private int dotColor;
	private int chessmanColor1;
	private float chessmanRadius1;
	private int chessmanColor2;
	private float chessmanRadius2;

	//记录onDraw被调用次数
	int count = 0;
	int mItemSpace = 0;

	public Gobang(Context context) {
		this(context,null);
	}

	public Gobang(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, -1,-1);
	}

	/*public Gobang(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr,-1);
		init(context, attrs, defStyleAttr,-1);
	}
*/
/*	public Gobang(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context,attrs,defStyleAttr,defStyleRes);
	}*/

	//初始化操作：画笔、自定义属性、
	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		// RefWatcher refWatcher = MyAPP.getRefWatcher(getContext());
		// refWatcher.watch(this);

		//初始化自定义属性
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.Gobang);
		standard = ta.getInt(R.styleable.Gobang_standard,DEFSTANDARD);
		//backgroud = ta.getResourceId()
		lineColor = ta.getColor(R.styleable.Gobang_line_color,DEFLINECOLOR);
		lineWidth = ta.getDimension(R.styleable.Gobang_line_width,DEFLINEWIDTH);
		dotColor = ta.getColor(R.styleable.Gobang_dot_color,DEFDOTCOLOR);
		dotRadius = ta.getFloat(R.styleable.Gobang_dot_radius,DEFDOTRADIUS);
		chessmanColor1 = ta.getColor(R.styleable.Gobang_chessman_color1,DEFCHESSMANCOLOR1);
		chessmanRadius1 = ta.getFloat(R.styleable.Gobang_chessman_radius1,DEFCHESSMANRADIUS1);
		chessmanColor2 = ta.getColor(R.styleable.Gobang_chessman_color2,DEFCHESSMANCOLOR2);
		chessmanRadius2 = ta.getFloat(R.styleable.Gobang_chessman_radius2,DEFCHESSMANRADIUS2);

		ta.recycle();
		// 初始化画笔
		mPaint = new Paint();
		mPaint.setStrokeWidth(DEFLINEWIDTH);
		mPaint.setColor(DEFLINECOLOR);
		mPaint.setAntiAlias(true);
		//初始化小圆点坐标集合
		dots = new ArrayList<>();
		totalDots = new ArrayList<>();
		mustDots = new ArrayList<>();

		initDots();
	}

	private void initDots() {
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// super.onDraw(canvas);

		//根据路径画标题
		drawTitle(canvas);

		// 画背景
		drawBg(canvas);
		//画格子
		drawGrid(canvas);
		// 画中心点
		count++;

		Log.e("count = ", String.valueOf(count));
		if (standard%4==0 || standard == 14){  //偶数是需要绘制中心点
			drawCentrePoint(canvas);
		}
		// 画棋子
		if (mustDots!=null&&mustDots.size()>0){
			//每次绘制集合中的最后一个点
			int pIndex = mustDots.size()-1;
			if (pIndex<totalDots.size()){
				//判断如果绘制的点已经在集合中则不再重复绘制
				Map<String, Integer> pDots = mustDots.get(pIndex);
				//
				ArrayList<Map<String, Integer>> copyMustDots = new ArrayList<>();
				copyMustDots.addAll(mustDots);
				copyMustDots.remove(copyMustDots.size()-1);

				if (!copyMustDots.contains(pDots)){
					drawPreDots(canvas, copyMustDots);
					drawChessman(canvas,pDots.get("X"),pDots.get("Y"),pDots.get("color"));
					//保存状态
					// canvas.save();	//保存之后能够调用Canvas的平移、放缩、旋转、裁剪等操作
				}else {
					//恢复保存的状态
					// canvas.restore();
					drawPreDots(canvas, copyMustDots);
					Toast.makeText(getContext(),"请选择正确的位置摆放棋子",Toast.LENGTH_SHORT).show();
				}
			}else {
				Toast.makeText(getContext(),"游戏已经结束",Toast.LENGTH_SHORT).show();
				//清空棋子重新绘制界面
				mustDots.clear();
				invalidate();
			}
		}
		/*int random = (int) Math.abs(Math.random()+10);
		for (int i = 0; i < totalDots.size(); i = i+random) {
			Map<String, Integer> map = totalDots.get(i);
			for (int j = 0; j < totalDots.size(); j = j+random) {
				Map<String, Integer> map1 = totalDots.get(j);
				//canvas.drawCircle(map.get("X"),map1.get("Y"),dotRadius,mPaint);
				drawChessman(canvas,map.get("X"),map1.get("Y"));
			}
		}*/

		Log.e("count = ", String.valueOf(count));
	}

	private void drawTitle(Canvas canvas) {
		mPaint.setColor(Color.RED);
		mPaint.setStrokeWidth(1);
		//设置画笔空心
		mPaint.setStyle(Paint.Style.STROKE);
		//先绘制一个圆形
		canvas.drawCircle(canvas.getWidth()/2,100,80,mPaint);
		//绘制作者名称
		String auther = "魔·笛";
		//通过文字构造一个矩形
		Rect rect = new Rect();
		mPaint.getTextBounds(auther,0,auther.length(), rect);
		int	width = rect.width();
		// width = (int) mPaint.measureText(auther);
		int height = rect.height();
		mPaint.setTextSize(23);
		canvas.drawText(auther,(canvas.getWidth()-width)/2,100,mPaint);
		Path path = new Path();
		path.addCircle(canvas.getWidth()/2,100,80, Path.Direction.CCW);
		//根据圆形绘制文字
		mPaint.setTextSize(20);
		canvas.drawTextOnPath("自定义View之五子棋UI的实现",path, (float) ((2*Math.PI*80)/2),20,mPaint);
	}

	private void drawPreDots(Canvas canvas, ArrayList<Map<String, Integer>> copyMustDots) {
		for (int i = 0; i < copyMustDots.size(); i++) {
			Map<String, Integer> map = copyMustDots.get(i);
			drawChessman(canvas,map.get("X"),map.get("Y"),map.get("color"));
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		//Log.e("触摸点坐标",event.getX()+":"+event.getY());
		//float rawX = event.getRawX();
		//float rawY = event.getRawY();
		float rawX = event.getX();
		float rawY = event.getY();
		switch (action){
			case MotionEvent.ACTION_DOWN:
				Log.e("ACTION_DOWN触摸点坐标",rawX+":"+rawY);
				boolean isContain = isContainInTotalDots(rawX, rawY);
				Log.e("是否包含该坐标点 ：", String.valueOf(isContain));
				if (isContain){
					//drawChessman(mCanvas,rawX,rawY);
					//重新绘制界面
					invalidate();
					//postInvalidate();
					return true;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				Log.e("ACTION_MOVE触摸点坐标",rawX+":"+rawY);
				break;
			case MotionEvent.ACTION_UP:
				Log.e("ACTION_UP触摸点坐标",rawX+":"+rawY);
				break;
			case MotionEvent.ACTION_CANCEL:
				Log.e("ACTION_CANCEL触摸点坐标",rawX+":"+rawY);
				break;
		}
		/*if (action == MotionEvent.ACTION_UP){
			Toast.makeText(getContext(),"触摸事件生效",Toast.LENGTH_SHORT).show();
			//判断当前坐标位置是否在坐标点中
			float x = event.getX();
			float y = event.getY();
			float rawX = event.getRawX();
			float rawY = event.getRawY();
			boolean isContain = isContainInTotalDots(rawX, rawY);
			if (isContain && mCanvas!=null){
				drawChessman(mCanvas,rawX,rawY);
				return true;
			}
		}*/
		return false;
	}

	private boolean isContainInTotalDots(float rawX, float rawY) {
		boolean xContain = false;
		boolean yContain = false;
		boolean isContain = false;
		int rX = (int) rawX;
		int rY = (int) rawY;

		int x = 0;
		int y = 0;
		if (totalDots!=null&&totalDots.size()>0){

			for (int i = 0; i < totalDots.size(); i++) {
				Map<String, Integer> map = totalDots.get(i);
				x = map.get("X");
				/*if (rX>x-5&&rX<x+5){
					xContain = true;
				}*/
				for (int j = 0; j < totalDots.size(); j++) {
					Map<String, Integer> map1 = totalDots.get(j);
					// x = map.get("X");
					y = map1.get("Y");

					//这种方式行不通 因为每次都会找到最后一个符合要求的y值：
					// 而且符合要求的y值是一条线故每次都是画到最后一条线上
					/*if (rX>x-5&&rX<x+5){
						xContain = true;
					}
					if (rY>y-5&&rY<y+5){
						yContain = true;
					}*/

					Rect rect = new Rect(x - mItemSpace / 2, y - mItemSpace / 2, x + mItemSpace / 2, y + mItemSpace / 2);
					if (rect.contains(rX,rY)){
						HashMap<String, Integer> map2 = new HashMap<>();
						map2.put("X",x);
						map2.put("Y",y);
						//记录每个棋子颜色
						if (isChange){
							map2.put("color",chessmanColor1);
							isChange = !isChange;
						}else {
							map2.put("color",chessmanColor2);
							isChange = !isChange;
						}
						mustDots.add(map2);
						isContain = true;
						return true;
					}
				}
				/*if (xContain && yContain){
					mustDots.put("X",x);
					mustDots.put("Y",y);
					Log.e("符合要求的XY","X = "+x+"  Y = "+y);
					return true;
				}*/
			}

			/*if (xContain && yContain){
				mustDots.put("X",x);
				mustDots.put("Y",y);
				return true;
			}*/
			/*for (int i = 0; i < totalDots.size(); i++) {
				Log.e("totalDots大小", String.valueOf(totalDots.size()));
				Map<String, Integer> map = totalDots.get(i);
				int x = map.get("X");
				int y = map.get("Y");
				if (rawX>x-5&&rawX<x+5){
					xContain = true;
				}
				if (rawY>y-5&&rawY<y+5){
					yContain = true;
				}
				return xContain && yContain;
			}*/
		}
		return false;
	}

	private void drawBg(Canvas canvas) {
		// canvas.drawColor(DEFBACKGROUD);
		canvas.drawColor(0xfaffbd);
		/*mPaint.setTextSize(20);
		mPaint.setColor(Color.RED);
		mPaint.setTextAlign(Paint.Align.CENTER);
		mPaint.setStrokeWidth(10);
		canvas.drawText("出问题了",10,10,mPaint);*/
	}

	private void drawGrid(Canvas canvas) {
		//设置画笔样式
		mPaint.setColor(lineColor);
		mPaint.setStrokeWidth(lineWidth);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setAntiAlias(true);
		// 计算画线的条数及位置
		int lineCount = standard + 1;
		int position = 0;
		int orientation = 0;
		//画水平线
		drawHorizontalLine(canvas,mPaint,lineCount);
		//画垂直线
		drawVerticalLine(canvas,mPaint,lineCount);
	}

	private void drawHorizontalLine(Canvas canvas, Paint mPaint, int lineCount) {
		//需要绘制的是正方形：所以以宽为准：较短
		int cHeight = canvas.getHeight();
		int cWidth = canvas.getWidth();
		//计算出线条所占宽度
		float linesWidth = lineCount * mPaint.getStrokeWidth();
		int itemSpace = (int) ((cWidth-linesWidth) / lineCount-1);

		mItemSpace = itemSpace;
		int endPos = itemSpace*(lineCount-1);
		//计算出剩余部分使布局居中显示
		int offsetX = cWidth - endPos;
		int offsetY = cHeight - endPos;

		//计算出中心点坐标供绘制时使用
		if (standard % 2 == 0){
			cPivotX = (endPos)/2+offsetX/2;
		}
		if (standard == 14){
			HashMap<String, Integer> map1 = new HashMap<>();
			HashMap<String, Integer> map2 = new HashMap<>();
			HashMap<String, Integer> map3 = new HashMap<>();
			HashMap<String, Integer> map4 = new HashMap<>();
			map1.put("dotX",offsetX/2+3*itemSpace);
			map1.put("dotY",offsetY/2+3*itemSpace);
			map2.put("dotX",offsetX/2+11*itemSpace);
			map2.put("dotY",offsetY/2+3*itemSpace);
			map3.put("dotX",offsetX/2+3*itemSpace);
			map3.put("dotY",offsetY/2+11*itemSpace);
			map4.put("dotX",offsetX/2+11*itemSpace);
			map4.put("dotY",offsetY/2+11*itemSpace);
			dots.add(map1);
			dots.add(map2);
			dots.add(map3);
			dots.add(map4);
		}

		for (int i = 0; i < lineCount; i++) {
			/*if (i == 0 || i == lineCount-1){
				mPaint.setStrokeWidth(3);
				mPaint.setStyle(Paint.Style.STROKE);
			}else {
				mPaint.setStrokeWidth(1);
				mPaint.setStyle(Paint.Style.FILL);
			}*/
			canvas.drawLine(offsetX/2,i*itemSpace+offsetY/2,endPos+offsetX/2,i*itemSpace+offsetY/2,mPaint);
			//canvas.drawLine(i*itemSpace+offsetX/2,offsetY/2,i*itemSpace+offsetX/2,endPos+offsetY/2,mPaint);
		}

		//保存所以的坐标点集合
		if (totalDots.size()==0){
			totalDots.clear();
			for (int i = 0; i < lineCount; i++) {
				for (int j = 0; j < lineCount; j++) {
					HashMap<String, Integer> map = new HashMap<>();
					map.put("X",j*itemSpace+offsetX/2);
					map.put("Y",j*itemSpace+offsetY/2);
					Log.e("i = ", String.valueOf(i));
					totalDots.add(map);
				}
			}
		}
	}

	private void drawVerticalLine(Canvas canvas, Paint mPaint, int lineCount) {
		int cHeight = canvas.getHeight();
		int cWidth = canvas.getWidth();
		//计算出线条所占宽度
		float linesWidth = lineCount * mPaint.getStrokeWidth();
		int itemSpace = (int) ((cWidth-linesWidth) / lineCount-1);
		int endPos = itemSpace*(lineCount-1);
		//计算出剩余部分使布局居中显示
		int offsetX = cWidth - endPos;
		int offsetY = cHeight - endPos;

		//计算出中心点坐标供绘制时使用
		if (standard % 2 == 0){
			cPivotY = (endPos)/2+offsetY/2;
		}

		for (int i = 0; i < lineCount; i++) {
			/*if (i == 0 || i == lineCount-1){
				mPaint.setStrokeWidth(3);
				mPaint.setStyle(Paint.Style.STROKE);
			}else {
				mPaint.setStrokeWidth(1);
				mPaint.setStyle(Paint.Style.FILL);
			}*/
			canvas.drawLine(i*itemSpace+offsetX/2,offsetY/2,i*itemSpace+offsetX/2,endPos+offsetY/2,mPaint);
		}
	}

	private void drawCentrePoint(Canvas canvas) {
		mPaint.setColor(dotColor);
		mPaint.setStrokeWidth(5);
		mPaint.setAntiAlias(true);
		if (cPivotY != 0){
			canvas.drawCircle(cPivotX,cPivotY,dotRadius,mPaint);
		}
		//如果standard为14表示标准棋盘：绘制五个点
		if (dots!=null&&dots.size()>0){
			for (int i = 0; i < dots.size(); i++) {
				Map<String, Integer> map = dots.get(i);
				canvas.drawCircle(map.get("dotX"),map.get("dotY"),dotRadius,mPaint);
			}
		}
		/*for (int i = 0; i < totalDots.size(); i++) {
			Map<String, Integer> map = totalDots.get(i);
			for (int j = 0; j < totalDots.size(); j++) {
				Map<String, Integer> map1 = totalDots.get(j);
				canvas.drawCircle(map.get("X"),map1.get("Y"),dotRadius,mPaint);
			}
		}*/
	}

	private void drawChessman(Canvas canvas, float X, float Y,int color) {
		/*if (isChange){
			mPaint.setColor(chessmanColor1);
		}else {
			mPaint.setColor(chessmanColor2);
		}*/
		mPaint.setColor(color);
		mPaint.setStrokeWidth(5);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setAntiAlias(true);
		canvas.drawCircle(X,Y,chessmanRadius1,mPaint);
		// isChange = !isChange;
	}
}
