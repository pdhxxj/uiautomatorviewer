/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.uiautomator;

import com.android.uiautomator.actions.ExpandAllAction;
import com.android.uiautomator.actions.ImageHelper;
import com.android.uiautomator.actions.ScreenshotAction;
import com.android.uiautomator.actions.ToggleNafAction;

import com.android.uiautomator.tree.AttributePair;
import com.android.uiautomator.tree.BasicTreeNode;
import com.android.uiautomator.tree.BasicTreeNodeContentProvider;
import com.android.uiautomator.tree.UiNode;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;


import java.io.File;
import java.io.IOException;
import java.util.List;

public class UiAutomatorView extends Composite {
    private static final int IMG_BORDER = 2;

    // The screenshot area is made of a stack layout of two components: screenshot canvas and
    // a "specify screenshot" button. If a screenshot is already available, then that is displayed
    // on the canvas. If it is not availble, then the "specify screenshot" button is displayed.
    private Composite mScreenshotComposite;
    private StackLayout mStackLayout;
    private Composite mSetScreenshotComposite;
    private Canvas mScreenshotCanvas;

    private TreeViewer mTreeViewer;
    private TableViewer mTableViewer;

    private float mScale = 1.0f;
    private int mDx, mDy;

    private UiAutomatorModel mModel;
    private File mModelFile;
    private Image mScreenshot;

    private List<BasicTreeNode> mSearchResult;
    private int mSearchResultIndex;
    private ToolItem itemDeleteAndInfo;
    private Text searchTextarea;
    private Text scriptTextarea;
    private Cursor mOrginialCursor;
    private ToolItem itemPrev, itemNext;
    private ToolItem coordinateLabel;

    private String mLastSearchedTerm;
    
    private Cursor mCrossCursor;
    
    public UiAutomatorView(Composite parent, int style) {
        super(parent, SWT.NONE);
        setLayout(new FillLayout());

        SashForm baseSash = new SashForm(this, SWT.HORIZONTAL);
    
        mOrginialCursor = getShell().getCursor();
        mCrossCursor = new Cursor(getDisplay(), SWT.CURSOR_CROSS);
        mScreenshotComposite = new Composite(baseSash, SWT.BORDER);
        mStackLayout = new StackLayout();
        mScreenshotComposite.setLayout(mStackLayout);
        // draw the canvas with border, so the divider area for sash form can be highlighted
        mScreenshotCanvas = new Canvas(mScreenshotComposite, SWT.BORDER);
        mStackLayout.topControl = mScreenshotCanvas;
        mScreenshotComposite.layout();

        // set cursor when enter canvas
        mScreenshotCanvas.addListener(SWT.MouseEnter, new Listener() {
            @Override
            public void handleEvent(Event arg0) {
                getShell().setCursor(mCrossCursor);
            }
        });
        mScreenshotCanvas.addListener(SWT.MouseExit, new Listener() {
            @Override
            public void handleEvent(Event arg0) {
                getShell().setCursor(mOrginialCursor);
            }
        });

        mScreenshotCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                if (mModel != null) {
                    mModel.toggleExploreMode();
                    redrawScreenshot();
                }
                if(e.button==3){
                	//set menus
                	Menu menu=new Menu(mScreenshotCanvas);
                	mScreenshotCanvas.setMenu(menu);               	
                	
                	Menu menu1=new Menu(menu);
                	Menu menu2=new Menu(menu);
                	Menu menu3=new Menu(menu);
                	Menu menu4=new Menu(menu);
                	Menu menu5=new Menu(menu);
                	Menu menu6=new Menu(menu);
                	Menu menu7=new Menu(menu);
                	Menu menu8=new Menu(menu);
                	Menu menu9=new Menu(menu);
                	
                	//set items
                	MenuItem item1=new MenuItem(menu,SWT.CASCADE);
                	MenuItem item2=new MenuItem(menu,SWT.CASCADE);
                	MenuItem item3=new MenuItem(menu,SWT.CASCADE);
                	MenuItem item4=new MenuItem(menu,SWT.CASCADE);
                	MenuItem item5=new MenuItem(menu,SWT.CASCADE);
                	MenuItem item6=new MenuItem(menu,SWT.CASCADE);
                	MenuItem item7=new MenuItem(menu,SWT.CASCADE);
                	MenuItem item8=new MenuItem(menu,SWT.CASCADE);
                	MenuItem item9=new MenuItem(menu,SWT.CASCADE);
                	MenuItem item43=new MenuItem(menu,SWT.CASCADE);
                	MenuItem item10=new MenuItem(menu1,SWT.NONE);
                	MenuItem item11=new MenuItem(menu1,SWT.NONE);
                	MenuItem item12=new MenuItem(menu1,SWT.NONE);
                	MenuItem item13=new MenuItem(menu1,SWT.NONE);
                	MenuItem item14=new MenuItem(menu1,SWT.NONE);
                	MenuItem item15=new MenuItem(menu2,SWT.NONE);
                	MenuItem item16=new MenuItem(menu2,SWT.NONE);
                	MenuItem item17=new MenuItem(menu2,SWT.NONE);
                	MenuItem item18=new MenuItem(menu2,SWT.NONE);
                	MenuItem item19=new MenuItem(menu2,SWT.NONE);
                	MenuItem item20=new MenuItem(menu3,SWT.NONE);
                	MenuItem item21=new MenuItem(menu3,SWT.NONE);
                	MenuItem item22=new MenuItem(menu3,SWT.NONE);
                	MenuItem item23=new MenuItem(menu3,SWT.NONE);
                	MenuItem item24=new MenuItem(menu3,SWT.NONE);
                	MenuItem item25=new MenuItem(menu4,SWT.NONE);
                	MenuItem item26=new MenuItem(menu4,SWT.NONE);
                	MenuItem item27=new MenuItem(menu4,SWT.NONE);
                	MenuItem item28=new MenuItem(menu4,SWT.NONE);
                	MenuItem item29=new MenuItem(menu4,SWT.NONE);
                	MenuItem item30=new MenuItem(menu6,SWT.NONE);
                	MenuItem item31=new MenuItem(menu6,SWT.NONE);
                	MenuItem item32=new MenuItem(menu6,SWT.NONE);
                	MenuItem item33=new MenuItem(menu5,SWT.NONE);
                	MenuItem item34=new MenuItem(menu5,SWT.NONE);
                	MenuItem item35=new MenuItem(menu5,SWT.NONE);
                	MenuItem item36=new MenuItem(menu5,SWT.NONE);
                	MenuItem item37=new MenuItem(menu5,SWT.NONE);
                 	MenuItem item38=new MenuItem(menu8,SWT.NONE);
                	MenuItem item39=new MenuItem(menu8,SWT.NONE);
                	MenuItem item40=new MenuItem(menu8,SWT.NONE);
                	MenuItem item41=new MenuItem(menu8,SWT.NONE);
                	MenuItem item42=new MenuItem(menu8,SWT.NONE);
                	MenuItem item44=new MenuItem(menu6,SWT.NONE);
                	//set item text
                	item1.setText("Click");
                	item2.setText("Click(Refresh)");
                	item3.setText("longClick");
                	item4.setText("longClick(Refresh)");
                	item5.setText("editText");
                	item6.setText("SystemCommand");
                	item7.setText("SystemCommand(Refresh)");
                	item43.setText("Sleep");
                	item8.setText("Check");
                	item9.setText("Find");
                	item10.setText("id");
                	item11.setText("text");
                	item12.setText("desc");
                	item13.setText("class");
                	item14.setText("xpath");
                	item15.setText("id");
                	item16.setText("text");
                	item17.setText("desc");
                	item18.setText("class");
                	item19.setText("xpath");
                	item20.setText("id");
                	item21.setText("text");
                	item22.setText("desc");
                	item23.setText("class");
                	item24.setText("xpath");
                	item25.setText("id");
                	item26.setText("text");
                	item27.setText("desc");
                	item28.setText("class");
                	item29.setText("xpath");
                	item30.setText("Home");
                	item31.setText("Back");
                	item32.setText("Power");
                	item33.setText("id");
                	item34.setText("text");
                	item35.setText("desc");
                	item36.setText("class");
                	item37.setText("xpath");
                	item38.setText("id");
                	item39.setText("text");
                	item40.setText("desc");
                	item41.setText("class");
                	item42.setText("xpath");
                	item44.setText("Other");
                	//bind menu
                	item1.setMenu(menu1);
                	//item2.setMenu(menu2);
                	item3.setMenu(menu3);
                	//item4.setMenu(menu4);
                	item5.setMenu(menu5);
                	item6.setMenu(menu6);
                	//item7.setMenu(menu7);
                	item8.setMenu(menu8);
                	//item9.setMenu(menu9);                	        
                	
                	//add item listener
                	//click item10-14
                	item10.addSelectionListener(new SelectionAdapter(){
                		@Override
                		public void widgetSelected(SelectionEvent e){
                			String script=getScriptByAction(item10.getText(),item1.getText());
                			chargeText(script);
                			//scriptTextarea.setText(script);               			
                		}
                	});
                	
                	item11.addSelectionListener(new SelectionAdapter(){
                			@Override
							public void widgetSelected(SelectionEvent e){
                				String script=getScriptByAction(item11.getText(),item1.getText());
                				chargeText(script);
                    			
                		}
                	});
                	item12.addSelectionListener(new SelectionAdapter(){
            			@Override
						public void widgetSelected(SelectionEvent e){
            				String script=getScriptByAction(item12.getText(),item1.getText());
            				chargeText(script);        			
            			}
                	});
                	item13.addSelectionListener(new SelectionAdapter(){
            			@Override
						public void widgetSelected(SelectionEvent e){           				
            				String script=getScriptByAction(item13.getText(),item1.getText());
            				chargeText(script);            			
            			}
                	});
                	item14.addSelectionListener(new SelectionAdapter(){
            			@Override
						public void widgetSelected(SelectionEvent e){
            				String script=getScriptByAction(item14.getText(),item1.getText());
            				chargeText(script);           			           			
            			}
                	});
                	
                	//click(refresh) item15-19
                	
                 	item2.addSelectionListener(new SelectionAdapter(){
            			@Override
						public void widgetSelected(SelectionEvent e){
            				objectClick(item2.getText(),"");
            				UiAutomatorViewer window=UiAutomatorViewer.getInstance();
            				ScreenshotAction screenshot=new ScreenshotAction(window, false);
            				screenshot.run();
            			}
                	});
                	
                	//longclick item20-24
                	item20.addSelectionListener(new SelectionAdapter(){
            			@Override
						public void widgetSelected(SelectionEvent e){
            				String script=getScriptByAction(item20.getText(),item3.getText());
            				chargeText(script);           			           			
            			}
                	});
                 	item21.addSelectionListener(new SelectionAdapter(){
            			@Override
						public void widgetSelected(SelectionEvent e){
            				String script=getScriptByAction(item21.getText(),item3.getText());
            				chargeText(script);           			           			
            			}
                	});
                 	item22.addSelectionListener(new SelectionAdapter(){
            			@Override
						public void widgetSelected(SelectionEvent e){
            				String script=getScriptByAction(item22.getText(),item3.getText());
            				chargeText(script);           			           			
            			}
                	});
                 	item23.addSelectionListener(new SelectionAdapter(){
            			@Override
						public void widgetSelected(SelectionEvent e){
            				String script=getScriptByAction(item23.getText(),item3.getText());
            				chargeText(script);           			           			
            			}
                	});
                 	item24.addSelectionListener(new SelectionAdapter(){
            			@Override
						public void widgetSelected(SelectionEvent e){
            				String script=getScriptByAction(item24.getText(),item3.getText());
            				chargeText(script);           			           			
            			}
                	});
                	
                 	//longclick(refresh)
                 	item4.addSelectionListener(new SelectionAdapter(){
            			@Override
						public void widgetSelected(SelectionEvent e){
            				objectClick(item4.getText(),"");
            				UiAutomatorViewer window=UiAutomatorViewer.getInstance();
            				ScreenshotAction screenshot=new ScreenshotAction(window, false);
            				screenshot.run();
            			}

						
                	});
                 	
                	//edittext item33-37
                	item33.addSelectionListener(new SelectionAdapter(){
                		@Override
                		public void widgetSelected(SelectionEvent e){
                			InputDialog dialog=new InputDialog(getShell(),"please input id","please input",null,null);
                			if(dialog.open()==InputDialog.OK){
                				String script=getScriptByValue(item33.getText(),dialog.getValue());
                				chargeText(script);
                			}               			             			
                		}
                	});
                	item34.addSelectionListener(new SelectionAdapter(){
                		@Override
                		public void widgetSelected(SelectionEvent e){
                			InputDialog dialog=new InputDialog(getShell(),"please input text","please input",null,null);
                			if(dialog.open()==InputDialog.OK){
                				String script=getScriptByValue(item34.getText(),dialog.getValue());
                				chargeText(script);
                			}               			             			
                		}
                	});
                	item35.addSelectionListener(new SelectionAdapter(){
                		@Override
                		public void widgetSelected(SelectionEvent e){
                			InputDialog dialog=new InputDialog(getShell(),"please input desc","please input",null,null);
                			if(dialog.open()==InputDialog.OK){
                				String script=getScriptByValue(item35.getText(),dialog.getValue());
                				chargeText(script);
                			}               			             			
                		}
                	});
                	item36.addSelectionListener(new SelectionAdapter(){
                		@Override
                		public void widgetSelected(SelectionEvent e){
                			InputDialog dialog=new InputDialog(getShell(),"please input class","please input",null,null);
                			if(dialog.open()==InputDialog.OK){
                				String script=getScriptByValue(item36.getText(),dialog.getValue());
                				chargeText(script);
                			}               			             			
                		}
                	});
                	item37.addSelectionListener(new SelectionAdapter(){
                		@Override
                		public void widgetSelected(SelectionEvent e){
                			InputDialog dialog=new InputDialog(getShell(),"please input xpath","please input",null,null);
                			if(dialog.open()==InputDialog.OK){
                				String script=getScriptByValue(item37.getText(),dialog.getValue());
                				chargeText(script);
                			}               			             			
                		}
                	});
                	//systemcommand
                	item30.addSelectionListener(new SelectionAdapter(){
            			@Override
						public void widgetSelected(SelectionEvent e){
            				String script=getScriptByCommand(item30.getText(),"3");
            				chargeText(script);
            			}
                	});
                	item31.addSelectionListener(new SelectionAdapter(){
            			@Override
						public void widgetSelected(SelectionEvent e){
            				String script=getScriptByCommand(item31.getText(),"4");
            				chargeText(script);
            			}
                	});
                	item32.addSelectionListener(new SelectionAdapter(){
            			@Override
						public void widgetSelected(SelectionEvent e){
            				String script=getScriptByCommand(item32.getText(),"26");
            				chargeText(script);
            			}
                	});
                	item44.addSelectionListener(new SelectionAdapter(){
            			@Override
						public void widgetSelected(SelectionEvent e){
            				InputDialog dialog=new InputDialog(getShell(),"please input text","please input",null,null);                			
            				if(dialog.open()==InputDialog.OK&&dialog.getValue()!=""){
                				String script=getScriptByCommand(item44.getText(),dialog.getValue());
                				chargeText(script);
                			}
            				else{
            					//MessageDialog mdialog=new MessageDialog(getShell(), null, null, "please enter text", MessageDialog.WARNING, null, MessageDialog.WARNING);
            					//mdialog.open();
            					dialog.open();
            				}
            			}
                	});
                	
                	//systemcommand(refresh)
                	item7.addSelectionListener(new SelectionAdapter(){
            			@Override
						public void widgetSelected(SelectionEvent e){
            				InputDialog dialog=new InputDialog(getShell(),"please input keyevent","please input",null,null);
            				if(dialog.open()==InputDialog.OK){
            					objectClick(item7.getText(),dialog.getValue());
            					UiAutomatorViewer window=UiAutomatorViewer.getInstance();
                				ScreenshotAction screenshot=new ScreenshotAction(window, false);
                				screenshot.run();
                			}
            			}
                	});
                }
            }
            

            
            
            
            private String getRes(String res){
            	String Res=((UiNode)mModel.getSelectedNode()).getAttribute(res);        	            	
				return Res;
            	
            }
                       
            
			private String getScriptByAction(String id,String ac) {
				// TODO Auto-generated methoad stub
				String script="driver";
				String res="";
				switch(id){
					case "id":
						res=getRes("resource-id");
						script+=".findElementById(\""+res+"\").";
						script+=chargeAction(ac);
						break;
					case "text":
						res=getRes("text");
						script+=".findElementByText(\""+res+"\").";
						script+=chargeAction(ac);
						break;
					case "class":
						res=getRes("class");
						script+=".findElementByClassName(\""+res+"\").";
						script+=chargeAction(ac);
						break;
					case "desc":
						res=getRes("content-desc");
						script+=".findElementByContent(\""+res+"\").";
						script+=chargeAction(ac);	
						break;
					case "xpath":
						res=getRes("xpath");
						script+=".findElementByXpath(\""+res+"\").";
						script+=chargeAction(ac);	
						break;					
				}
				return script;
			}
			
			private String getScriptByCommand(String id,String value){
				String script="driver";
				switch(id){
				case "Home":					
					script+=".sendKeys(\""+value+"\")";
					break;
				case "Back":
					script+=".sendKeys(\""+value+"\")";
					break;
				case "Power":
					script+=".sendKeys(\""+value+"\")";
					break;
				case "Other":
					script+=".sendKeys(\""+value+"\")";	
					break;								
			}
			return script;
			}
			
			private String getScriptByValue(String id,String value) {
				// TODO Auto-generated method stub
				String script="driver";
				String res="";
				switch(id){
					case "id":
						res=getRes("resource-id");
						script+=".findElementById(\""+res+"\").sendKeys(\""+value+"\")";
						break;
					case "text":
						res=getRes("text");
						script+=".findElementByText(\""+res+"\").sendKeys(\""+value+"\")";
						break;
					case "class":
						res=getRes("class");
						script+=".findElementByClassName(\""+res+"\").sendKeys(\""+value+"\")";
						break;
					case "desc":
						res=getRes("content-desc");
						script+=".findElementByContent(\""+res+"\").sendKeys(\""+value+"\")";	
						break;
					case "xpath":
						res=getRes("xpath");
						script+=".findElementByXpath(\""+res+"\").sendKeys(\""+value+"\")";
						break;					
				}
				return script;
			}
			
			
			
			private  String chargeAction(String ac){
		    	String ca="";
		    	switch(ac){
		    		case "Click":
		    			ca="click();";
		    			break;		    		    				    		
		    		case "longClick":
		    			ca="longclick();";
		    			break;		    	
		    	}
		    	return ca;
		    }
			private void chargeText(String res){
				if(scriptTextarea.getText().isEmpty()){
					scriptTextarea.append(res);
				}
				else{
					scriptTextarea.append(System.getProperty("line.separator")+res);
				}
			}
			private void objectClick(String ac,String value) {
				Rectangle rect=mModel.getCurrentDrawingRect();
				String adbStr="";
				switch(ac){
					case "Click(Refresh)":
						adbStr="adb shell input tap "+(rect.x+rect.width/2)+" "+(rect.y+rect.height/2);
						break;
					case "LongClick(Refresh)":
						adbStr="adb shell input tap "+(rect.x+rect.width/2)+" "+(rect.y+rect.height/2);
						break;
					case "SystemCommand(Refresh)":
						adbStr="adb shell input keyevent "+value+"";
						break;
				}							
				try {
					System.out.println(adbStr);
					Runtime.getRuntime().exec(adbStr);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
			
        });
        mScreenshotCanvas.setBackground(
                getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        mScreenshotCanvas.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                if (mScreenshot != null) {
                    updateScreenshotTransformation();
                    // shifting the image here, so that there's a border around screen shot
                    // this makes highlighting red rectangles on the screen shot edges more visible
                    Transform t = new Transform(e.gc.getDevice());
                    t.translate(mDx, mDy);
                    t.scale(mScale, mScale);
                    e.gc.setTransform(t);
                    e.gc.drawImage(mScreenshot, 0, 0);
                    // this resets the transformation to identity transform, i.e. no change
                    // we don't use transformation here because it will cause the line pattern
                    // and line width of highlight rect to be scaled, causing to appear to be blurry
                    e.gc.setTransform(null);
                    if (mModel.shouldShowNafNodes()) {
                        // highlight the "Not Accessibility Friendly" nodes
                        e.gc.setForeground(e.gc.getDevice().getSystemColor(SWT.COLOR_YELLOW));
                        e.gc.setBackground(e.gc.getDevice().getSystemColor(SWT.COLOR_YELLOW));
                        for (Rectangle r : mModel.getNafNodes()) {
                            e.gc.setAlpha(50);
                            e.gc.fillRectangle(mDx + getScaledSize(r.x), mDy + getScaledSize(r.y),
                                    getScaledSize(r.width), getScaledSize(r.height));
                            e.gc.setAlpha(255);
                            e.gc.setLineStyle(SWT.LINE_SOLID);
                            e.gc.setLineWidth(2);
                            e.gc.drawRectangle(mDx + getScaledSize(r.x), mDy + getScaledSize(r.y),
                                    getScaledSize(r.width), getScaledSize(r.height));
                        }
                    }

                    // draw the search result rects
                    if (mSearchResult != null){
                        for (BasicTreeNode result : mSearchResult){
                            if (result instanceof UiNode) {
                                UiNode uiNode = (UiNode) result;
                                Rectangle rect = new Rectangle(
                                        uiNode.x, uiNode.y, uiNode.width, uiNode.height);
                                e.gc.setForeground(
                                        e.gc.getDevice().getSystemColor(SWT.COLOR_YELLOW));
                                e.gc.setLineStyle(SWT.LINE_DASH);
                                e.gc.setLineWidth(1);
                                e.gc.drawRectangle(mDx + getScaledSize(rect.x),
                                        mDy + getScaledSize(rect.y),
                                        getScaledSize(rect.width), getScaledSize(rect.height));
                            }
                        }
                    }

                    // draw the mouseover rects
                    Rectangle rect = mModel.getCurrentDrawingRect();
                    if (rect != null) {
                        e.gc.setForeground(e.gc.getDevice().getSystemColor(SWT.COLOR_RED));
                        if (mModel.isExploreMode()) {
                            // when we highlight nodes dynamically on mouse move,
                            // use dashed borders
                            e.gc.setLineStyle(SWT.LINE_DASH);
                            e.gc.setLineWidth(1);
                        } else {
                            // when highlighting nodes on tree node selection,
                            // use solid borders
                            e.gc.setLineStyle(SWT.LINE_SOLID);
                            e.gc.setLineWidth(2);
                        }
                        e.gc.drawRectangle(mDx + getScaledSize(rect.x), mDy + getScaledSize(rect.y),
                                getScaledSize(rect.width), getScaledSize(rect.height));
                    }
                }
            }
        });
        mScreenshotCanvas.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                if (mModel != null) {
                    int x = getInverseScaledSize(e.x - mDx);
                    int y = getInverseScaledSize(e.y - mDy);
                    // show coordinate
                    coordinateLabel.setText(String.format("(%d,%d)", x,y));
                    if (mModel.isExploreMode()) {
                        BasicTreeNode node = mModel.updateSelectionForCoordinates(x, y);
                        if (node != null) {
                            updateTreeSelection(node);
                        }
                    }
                }
            }
        });

        mSetScreenshotComposite = new Composite(mScreenshotComposite, SWT.NONE);
        mSetScreenshotComposite.setLayout(new GridLayout());

        final Button setScreenshotButton = new Button(mSetScreenshotComposite, SWT.PUSH);
        setScreenshotButton.setText("Specify Screenshot...");
        setScreenshotButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                FileDialog fd = new FileDialog(setScreenshotButton.getShell());
                fd.setFilterExtensions(new String[] {"*.png" });
                if (mModelFile != null) {
                    fd.setFilterPath(mModelFile.getParent());
                }
                String screenshotPath = fd.open();
                if (screenshotPath == null) {
                    return;
                }

                ImageData[] data;
                try {
                    data = new ImageLoader().load(screenshotPath);
                } catch (Exception e) {
                    return;
                }

                // "data" is an array, probably used to handle images that has multiple frames
                // i.e. gifs or icons, we just care if it has at least one here
                if (data.length < 1) {
                    return;
                }

                mScreenshot = new Image(Display.getDefault(), data[0]);
                redrawScreenshot();
            }
        });

        // right sash is split into 2 parts: upper-right and lower-right
        // both are composites with borders, so that the horizontal divider can be highlighted by
        // the borders
        SashForm rightSash = new SashForm(baseSash, SWT.VERTICAL);
       
        // upper-right base contains the toolbar and the tree
        Composite upperRightBase = new Composite(rightSash, SWT.BORDER);
        upperRightBase.setLayout(new GridLayout(1, false));

        ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
        toolBarManager.add(new ExpandAllAction(this));
        toolBarManager.add(new ToggleNafAction(this));
        ToolBar searchtoolbar = toolBarManager.createControl(upperRightBase);

        // add search box and navigation buttons for search results
        ToolItem itemSeparator = new ToolItem(searchtoolbar, SWT.SEPARATOR | SWT.RIGHT);
        searchTextarea = new Text(searchtoolbar, SWT.BORDER | SWT.SINGLE | SWT.SEARCH);
        searchTextarea.pack();
        itemSeparator.setWidth(searchTextarea.getBounds().width);
        itemSeparator.setControl(searchTextarea);
        itemPrev = new ToolItem(searchtoolbar, SWT.SIMPLE);
        itemPrev.setImage(ImageHelper.loadImageDescriptorFromResource("images/prev.png")
                .createImage());
        itemNext = new ToolItem(searchtoolbar, SWT.SIMPLE);
        itemNext.setImage(ImageHelper.loadImageDescriptorFromResource("images/next.png")
                .createImage());
        itemDeleteAndInfo = new ToolItem(searchtoolbar, SWT.SIMPLE);
        itemDeleteAndInfo.setImage(ImageHelper.loadImageDescriptorFromResource("images/delete.png")
                .createImage());
        itemDeleteAndInfo.setToolTipText("Clear search results");
        coordinateLabel = new ToolItem(searchtoolbar, SWT.SIMPLE);
        coordinateLabel.setText("");
        coordinateLabel.setEnabled(false);

        // add search function
        searchTextarea.addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyEvent event) {
                if (event.keyCode == SWT.CR) {
                    String term = searchTextarea.getText();
                    if (!term.isEmpty()) {
                        if (term.equals(mLastSearchedTerm)) {
                            nextSearchResult();
                            return;
                        }
                        clearSearchResult();
                        mSearchResult = mModel.searchNode(term);
                        if (!mSearchResult.isEmpty()) {
                            mSearchResultIndex = 0;
                            updateSearchResultSelection();
                            mLastSearchedTerm = term;
                        }
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent event) {
            }
        });
        SelectionListener l = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent se) {
                if (se.getSource() == itemPrev) {
                    prevSearchResult();
                 } else if (se.getSource() == itemNext) {
                    nextSearchResult();
                 } else if (se.getSource() == itemDeleteAndInfo) {
                    searchTextarea.setText("");
                    clearSearchResult();
                 }
            }
        };
        itemPrev.addSelectionListener(l);
        itemNext.addSelectionListener(l);
        itemDeleteAndInfo.addSelectionListener(l);

        searchtoolbar.pack();
        searchtoolbar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        mTreeViewer = new TreeViewer(upperRightBase, SWT.NONE);
        mTreeViewer.setContentProvider(new BasicTreeNodeContentProvider());
        // default LabelProvider uses toString() to generate text to display
        mTreeViewer.setLabelProvider(new LabelProvider());
        mTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                BasicTreeNode selectedNode = null;
                if (event.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                    Object o = selection.getFirstElement();
                    if (o instanceof BasicTreeNode) {
                        selectedNode = (BasicTreeNode) o;
                    }
                }

                mModel.setSelectedNode(selectedNode);
                redrawScreenshot();
                if (selectedNode != null) {
                    loadAttributeTable();
                }
            }
        });
        Tree tree = mTreeViewer.getTree();
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        // move focus so that it's not on tool bar (looks weird)
        tree.setFocus();

        // lower-right base contains the detail group
        Composite lowerRightBase = new Composite(rightSash, SWT.BORDER);
        lowerRightBase.setLayout(new FillLayout());
        Group grpNodeDetail = new Group(lowerRightBase, SWT.NONE);
        grpNodeDetail.setLayout(new FillLayout(SWT.HORIZONTAL));
        grpNodeDetail.setText("Node Detail");

        Composite tableContainer = new Composite(grpNodeDetail, SWT.NONE);

        TableColumnLayout columnLayout = new TableColumnLayout();
        tableContainer.setLayout(columnLayout);

        mTableViewer = new TableViewer(tableContainer, SWT.NONE | SWT.FULL_SELECTION);
        Table table = mTableViewer.getTable();
        table.setLinesVisible(true);
        // use ArrayContentProvider here, it assumes the input to the TableViewer
        // is an array, where each element represents a row in the table
        mTableViewer.setContentProvider(new ArrayContentProvider());

        TableViewerColumn tableViewerColumnKey = new TableViewerColumn(mTableViewer, SWT.NONE);
        TableColumn tblclmnKey = tableViewerColumnKey.getColumn();
        tableViewerColumnKey.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof AttributePair) {
                    // first column, shows the attribute name
                    return ((AttributePair) element).key;
                }
                return super.getText(element);
            }
        });
        columnLayout.setColumnData(tblclmnKey,
                new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));

        TableViewerColumn tableViewerColumnValue = new TableViewerColumn(mTableViewer, SWT.NONE);
        tableViewerColumnValue.setEditingSupport(new AttributeTableEditingSupport(mTableViewer));
        TableColumn tblclmnValue = tableViewerColumnValue.getColumn();
        columnLayout.setColumnData(tblclmnValue,
                new ColumnWeightData(2, ColumnWeightData.MINIMUM_WIDTH, true));
        tableViewerColumnValue.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof AttributePair) {
                    // second column, shows the attribute value
                    return ((AttributePair) element).value;
                }
                return super.getText(element);
            }
        });
        SashForm downSash = new SashForm(baseSash, SWT.VERTICAL | SWT.BORDER);       
        scriptTextarea = new Text(downSash, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
        downSash.setMaximizedControl(scriptTextarea);
        scriptTextarea.pack();
        scriptTextarea.addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyEvent event) {
                if (event.keyCode == SWT.CR) {
                    String lastterm = scriptTextarea.getText();
                    if (!lastterm.isEmpty()) {
                    	if(count==0){
                    		
                    		MessageDialog.openWarning(shell,"","");
                    		term=lastterm;
	                    	scriptTextarea.setText(term);
	                    	count++;
                    	}
                    	else{
                    		term+=lastterm;
                    		scriptTextarea.setText(term);
	                    	count++;
                    	}
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent event) {
            }
        });
        baseSash.setWeights(new int[] {4,3,3});
        // sets the ratio of the vertical split: left 5 vs right 3
       
        //baseSash.setWeights(new int[] {5,3});
    }
    final Shell shell = new Shell();
    private static int count=0;
    private static String term;
    
    protected void prevSearchResult() {
        if (mSearchResult == null)
            return;
        if(mSearchResult.isEmpty()){
            mSearchResult = null;
            return;
        }
        mSearchResultIndex = mSearchResultIndex - 1;
        if (mSearchResultIndex < 0){
            mSearchResultIndex += mSearchResult.size();
        }
        updateSearchResultSelection();
    }
    protected void clearSearchResult() {
        itemDeleteAndInfo.setText("");
        mSearchResult = null;
        mSearchResultIndex = 0;
        mLastSearchedTerm = "";
        mScreenshotCanvas.redraw();
    }
    protected void nextSearchResult() {
        if (mSearchResult == null)
            return;
        if(mSearchResult.isEmpty()){
            mSearchResult = null;
            return;
        }
        mSearchResultIndex = (mSearchResultIndex + 1) % mSearchResult.size();
        updateSearchResultSelection();
    }

    private void updateSearchResultSelection() {
        updateTreeSelection(mSearchResult.get(mSearchResultIndex));
        itemDeleteAndInfo.setText("" + (mSearchResultIndex + 1) + "/"
                + mSearchResult.size());
    }

    private int getScaledSize(int size) {
        if (mScale == 1.0f) {
            return size;
        } else {
            return new Double(Math.floor((size * mScale))).intValue();
        }
    }

    private int getInverseScaledSize(int size) {
        if (mScale == 1.0f) {
            return size;
        } else {
            return new Double(Math.floor((size / mScale))).intValue();
        }
    }

    private void updateScreenshotTransformation() {
        Rectangle canvas = mScreenshotCanvas.getBounds();
        Rectangle image = mScreenshot.getBounds();
        float scaleX = (canvas.width - 2 * IMG_BORDER - 1) / (float) image.width;
        float scaleY = (canvas.height - 2 * IMG_BORDER - 1) / (float) image.height;

        // use the smaller scale here so that we can fit the entire screenshot
        mScale = Math.min(scaleX, scaleY);
        // calculate translation values to center the image on the canvas
        mDx = (canvas.width - getScaledSize(image.width) - IMG_BORDER * 2) / 2 + IMG_BORDER;
        mDy = (canvas.height - getScaledSize(image.height) - IMG_BORDER * 2) / 2 + IMG_BORDER;
    }

    private class AttributeTableEditingSupport extends EditingSupport {

        private TableViewer mViewer;

        public AttributeTableEditingSupport(TableViewer viewer) {
            super(viewer);
            mViewer = viewer;
        }

        @Override
        protected boolean canEdit(Object arg0) {
            return true;
        }

        @Override
        protected CellEditor getCellEditor(Object arg0) {
            return new TextCellEditor(mViewer.getTable());
        }

        @Override
        protected Object getValue(Object o) {
            return ((AttributePair) o).value;
        }

        @Override
        protected void setValue(Object arg0, Object arg1) {
        }
    }

    /**
     * Causes a redraw of the canvas.
     *
     * The drawing code of canvas will handle highlighted nodes and etc based on data
     * retrieved from Model
     */
    public void redrawScreenshot() {
        if (mScreenshot == null) {
            mStackLayout.topControl = mSetScreenshotComposite;
        } else {
            mStackLayout.topControl = mScreenshotCanvas;
        }
        mScreenshotComposite.layout();

        mScreenshotCanvas.redraw();
    }

    public void setInputHierarchy(Object input) {
        mTreeViewer.setInput(input);
    }

    public void loadAttributeTable() {
        // update the lower right corner table to show the attributes of the node
        mTableViewer.setInput(mModel.getSelectedNode().getAttributesArray());
    }

    public void expandAll() {
        mTreeViewer.expandAll();
    }

    public void updateTreeSelection(BasicTreeNode node) {
        mTreeViewer.setSelection(new StructuredSelection(node), true);
    }

    public void setModel(UiAutomatorModel model, File modelBackingFile, Image screenshot) {
        mModel = model;
        mModelFile = modelBackingFile;

        if (mScreenshot != null) {
            mScreenshot.dispose();
        }
        mScreenshot = screenshot;
        clearSearchResult();
        redrawScreenshot();
        // load xml into tree
        BasicTreeNode wrapper = new BasicTreeNode();
        // putting another root node on top of existing root node
        // because Tree seems to like to hide the root node
        wrapper.addChild(mModel.getXmlRootNode());
        setInputHierarchy(wrapper);
        mTreeViewer.getTree().setFocus();

    }
    
    
    
    public boolean shouldShowNafNodes() {
        return mModel != null ? mModel.shouldShowNafNodes() : false;
    }

    public void toggleShowNaf() {
        if (mModel != null) {
            mModel.toggleShowNaf();
        }
    }

    public Image getScreenShot() {
        return mScreenshot;
    }

    public File getModelFile() {
        return mModelFile;
    }
}



