# Gobang
## 自定义View之五子棋UI实现
> ## 效果预览
> ![效果图1](https://github.com/JqyModi/Gobang/blob/master/xgt.png)
> ## 基本使用：
  > - 1.将项目下载到电脑
  > - 2.Gobang类复制到你自己的工程目录下
  > - 3.将values下的attrs文件复制到你自己的工程目录下
  > - 4.在布局文件中使用即可
> ## 基本实现步骤：
  > - 1.自定义View的基本操作不再累赘复述
  > - 2.不懂的看我另一篇JqyModi/ArcMenu：自定义弧形菜单实现讲得很细
  > - 3.下面主要针对onDraw、onTouch中的业务逻辑进行讲解：

> # 1.onDraw方法:
> - ①.绘制网格线:
```
  for (int i = 0; i < lineCount; i++) {
			canvas.drawLine(offsetX/2,i*itemSpace+offsetY/2,endPos+offsetX/2,i*itemSpace+offsetY/2,mPaint);
			//canvas.drawLine(i*itemSpace+offsetX/2,offsetY/2,i*itemSpace+offsetX/2,endPos+offsetY/2,mPaint);
		}
```
> - ②.绘制途中的五个小圆点
```
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
	}
```
> - ③.绘制棋子
```
	private void drawChessman(Canvas canvas, float X, float Y,int color) {
		mPaint.setColor(color);
		mPaint.setStrokeWidth(5);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setAntiAlias(true);
		canvas.drawCircle(X,Y,chessmanRadius1,mPaint);
		// isChange = !isChange;
	}
```
> # 2.onTouch方法:
> - ①.获取手指触摸点坐标
```
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
    }
```
> - ②.判断①中坐标是否包含在网格线的交点坐标中
```
			for (int i = 0; i < totalDots.size(); i++) {
				Map<String, Integer> map = totalDots.get(i);
				x = map.get("X");
				for (int j = 0; j < totalDots.size(); j++) {
					Map<String, Integer> map1 = totalDots.get(j);
					// x = map.get("X");
					y = map1.get("Y");
					//这种方式行不通 因为每次都会找到最后一个符合要求的y值：
					// 而且符合要求的y值是一条线故每次都是画到最后一条线上
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
			}
```
> - ③.重新绘制画布
```
    invalidate();
```
