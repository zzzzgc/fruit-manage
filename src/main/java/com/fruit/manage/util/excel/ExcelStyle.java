package com.fruit.manage.util.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public abstract class ExcelStyle extends ExcelBase {

    /**
     * 默认边框线样式
     */
    private static BorderStyle DEFAULT_BORDER = BorderStyle.THIN;

    /**
     * 通用:无边框居中样式
     *
     * @param wb 表格对象
     * @return XSSFCellStyle
     */
    public static XSSFCellStyle getNoBorderCenterStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 通用:无边框居左样式(默认)
     *
     * @param wb 表格对象
     * @return XSSFCellStyle
     */
    public static XSSFCellStyle getNoBorderStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }


    /**
     * 标题:多等级标题样式(默认居中)
     *
     * @param wb         XSSFWorkbook的Excel对象
     * @param titleLevel 标题等级
     * @return
     */
    public static XSSFCellStyle getStyleTitle(XSSFWorkbook wb, int titleLevel) {
        XSSFCellStyle style = getNoBorderCenterStyle(wb);
        XSSFFont font = getXssfFont(wb, titleLevel);
        style.setFont(font);
        return style;
    }

    /**
     * 文本:多等级是文本样式(默认居左)
     *
     * @param wb         XSSFWorkbook的Excel对象
     * @param titleLevel 标题等级
     * @return XSSFCellStyle excel的样式对象
     */
    public static XSSFCellStyle getStyleText(XSSFWorkbook wb, int titleLevel) {
        XSSFCellStyle style = getNoBorderStyle(wb);
        XSSFFont font = getXssfFont(wb, titleLevel);
        style.setFont(font);
        return style;
    }

    /**
     * 列表:上下边框列表单元格(居中)
     * --------------------------------
     * 表头    表头      表头       表头
     * --------------------------------
     * 内容    内容      内容       内容
     * --------------------------------
     * 内容    内容      内容       内容
     * ------------------------------
     * --
     *
     * @param wb
     * @param titleLevel
     * @return
     */
    public static XSSFCellStyle getStyleTableByOne(XSSFWorkbook wb, int titleLevel){

        XSSFCellStyle style = getNoBorderStyle(wb);
        style.setBorderTop(DEFAULT_BORDER);
        style.setBorderBottom(DEFAULT_BORDER);
        XSSFFont font = getXssfFont(wb, titleLevel);
        style.setFont(font);
        return style;
    }

    /**
     * 列表:全边框列表单元格(居中)
     * -------------------------------------
     * |表头  |  表头   |   表头    |   表头 |
     * -------------------------------------
     * |内容  |  内容   |   内容    |   内容 |
     * -------------------------------------
     * |内容  |  内容   |   内容    |   内容 |
     * -------------------------------------
     *
     * @param wb
     * @param titleLevel
     * @return
     */
    public static XSSFCellStyle getStyleTableByTwo(XSSFWorkbook wb, int titleLevel){
        XSSFCellStyle style = getNoBorderStyle(wb);
        style.setBorderTop(DEFAULT_BORDER);
        style.setBorderLeft(DEFAULT_BORDER);
        style.setBorderRight(DEFAULT_BORDER);
        style.setBorderBottom(DEFAULT_BORDER);
        XSSFFont font = getXssfFont(wb, titleLevel);
        style.setFont(font);
        return style;
    }

    /**
     * 获取默认格式的字体
     *
     * @param wb         XSSFWorkbook的Excel对象
     * @param titleLevel titleLevel = 1 size= 24 | titleLevel = 2 size= 18 | titleLevel = .....
     * @return XSSFFont excel的字体对象
     */
    public static XSSFFont getXssfFont(XSSFWorkbook wb, int titleLevel) {
        XSSFFont font = wb.createFont();
        font.setColor(Font.COLOR_NORMAL);
        font.setFontName("微软雅黑");
        font.setFontHeightInPoints((short) (24 - (titleLevel - 1) * 6));
        return font;
    }

    /**
     * 标题二 样式
     * 无边框,大字体
     *
     * @return XSSFCellStyle
     */
    protected XSSFCellStyle getStyleTitleForTwo(XSSFWorkbook wb) {
        XSSFCellStyle styleTitle = wb.createCellStyle();
        styleTitle.setAlignment(HorizontalAlignment.CENTER);
        styleTitle.setVerticalAlignment(VerticalAlignment.CENTER);
        styleTitle.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = getWorkbook().createFont();
        font.setColor(Font.COLOR_NORMAL);
        font.setFontHeightInPoints((short) 23);
        font.setFontName("宋体");
        styleTitle.setFont(font);
        return styleTitle;
    }




    /*
     * 标题和列名
     */

    /**
     * 标题样式【默认无边框】
     */
    private XSSFCellStyle styleTitle;
    /**
     * 列名样式【默认有边框】
     */
    private XSSFCellStyle styleHeader;


    /**
     * 标题样式【默认无边框】
     */
    protected XSSFCellStyle getStyleTitle() {
        if (styleTitle == null) {
            styleTitle = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleTitle.setAlignment(HorizontalAlignment.CENTER);
            styleTitle.setVerticalAlignment(VerticalAlignment.CENTER);
            styleTitle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 24);
            font.setFontName("宋体");
            styleTitle.setFont(font);
        }
        return styleTitle;
    }

    /**
     * 列名样式【默认有边框】
     */
    protected XSSFCellStyle getStyleHeader() {
        if (styleHeader == null) {
            styleHeader = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleHeader.setAlignment(HorizontalAlignment.CENTER);
            styleHeader.setVerticalAlignment(VerticalAlignment.CENTER);
            //边框
            styleHeader.setBorderBottom(ExcelUtil.BORDER); //下边框
            styleHeader.setBorderBottom(ExcelUtil.BORDER);
            styleHeader.setBorderLeft(ExcelUtil.BORDER);//左边框
            styleHeader.setBorderTop(ExcelUtil.BORDER);//上边框
            styleHeader.setBorderRight(ExcelUtil.BORDER);//右边框
            // 字体
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 12);
            font.setFontName("宋体");
            styleHeader.setFont(font);
        }
        return styleHeader;
    }


    /*
     * 字符串【Int也用这个】
     */


    /**
     * 字符串 【无边框,左边】
     */
    private XSSFCellStyle styleStrLeftNoBorder;
    /**
     * 字符串 【无边框，中间】
     */
    private XSSFCellStyle styleStrCenterNoBorder;
    /**
     * 字符串 【有边框，左边】
     */
    private XSSFCellStyle styleStrLeftWithBorder;
    /**
     * 字符串 【有边框，中间】
     */
    private XSSFCellStyle styleStrCenterWithBorder;


    /**
     * 字符串 【无边框,左边】
     */
    protected XSSFCellStyle getStyleStrLeftNoBorder() {
        if (styleStrLeftNoBorder == null) {
            styleStrLeftNoBorder = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleStrLeftNoBorder.setAlignment(HorizontalAlignment.LEFT);
            styleStrLeftNoBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleStrLeftNoBorder.setFont(font);
        }
        return styleStrLeftNoBorder;
    }

    /**
     * 字符串 【无边框，中间】
     */
    protected XSSFCellStyle getStyleStrCenterNoBorder() {
        if (styleStrCenterNoBorder == null) {
            styleStrCenterNoBorder = (XSSFCellStyle) getWorkbook().createCellStyle();


            styleStrCenterNoBorder.setAlignment(HorizontalAlignment.CENTER);
            styleStrCenterNoBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleStrCenterNoBorder.setFont(font);
        }
        return styleStrCenterNoBorder;
    }

    /**
     * 字符串 【有边框，左边】
     */
    protected XSSFCellStyle getStyleStrLeftWithBorder() {
        if (styleStrLeftWithBorder == null) {
            styleStrLeftWithBorder = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleStrLeftWithBorder.setAlignment(HorizontalAlignment.LEFT);
            styleStrLeftWithBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            //边框
            styleStrLeftWithBorder.setBorderBottom(ExcelUtil.BORDER); //下边框
            styleStrLeftWithBorder.setBorderLeft(ExcelUtil.BORDER);//左边框
            styleStrLeftWithBorder.setBorderTop(ExcelUtil.BORDER);//上边框
            styleStrLeftWithBorder.setBorderRight(ExcelUtil.BORDER);//右边框
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleStrLeftWithBorder.setFont(font);
        }
        return styleStrLeftWithBorder;
    }

    /**
     * 字符串 【有边框，中间】
     */
    protected XSSFCellStyle getStyleStrCenterWithBorder() {
        if (styleStrCenterWithBorder == null) {
            styleStrCenterWithBorder = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleStrCenterWithBorder.setAlignment(HorizontalAlignment.CENTER);
            styleStrCenterWithBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            //边框
            styleStrCenterWithBorder.setBorderBottom(ExcelUtil.BORDER); //下边框
            styleStrCenterWithBorder.setBorderLeft(ExcelUtil.BORDER);//左边框
            styleStrCenterWithBorder.setBorderTop(ExcelUtil.BORDER);//上边框
            styleStrCenterWithBorder.setBorderRight(ExcelUtil.BORDER);//右边框
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleStrCenterWithBorder.setFont(font);
        }
        return styleStrCenterWithBorder;
    }


    /*
     * 小数
     */


    /**
     * 小数 【无边框,左边】
     */
    private XSSFCellStyle styleNumLeftNoBorder;
    /**
     * 小数 【无边框，中间】
     */
    private XSSFCellStyle styleNumCenterNoBorder;
    /**
     * 小数 【有边框，左边】
     */
    private XSSFCellStyle styleNumLeftWithBorder;
    /**
     * 小数 【有边框，中间】
     */
    private XSSFCellStyle styleNumCenterWithBorder;


    /**
     * 小数 【无边框,左边】
     */
    protected XSSFCellStyle getStyleNumLeftNoBorder() {
        if (styleNumLeftNoBorder == null) {
            styleNumLeftNoBorder = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleNumLeftNoBorder.setAlignment(HorizontalAlignment.LEFT);
            styleNumLeftNoBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            styleNumLeftNoBorder.setDataFormat(getWorkbook().createDataFormat().getFormat("0.00"));
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleNumLeftNoBorder.setFont(font);
        }
        return styleNumLeftNoBorder;
    }

    /**
     * 小数 【无边框，中间】
     */
    protected XSSFCellStyle getStyleNumCenterNoBorder() {
        if (styleNumCenterNoBorder == null) {
            styleNumCenterNoBorder = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleNumCenterNoBorder.setAlignment(HorizontalAlignment.CENTER);
            styleNumCenterNoBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            styleNumCenterNoBorder.setDataFormat(getWorkbook().createDataFormat().getFormat("0.00"));
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleNumCenterNoBorder.setFont(font);
        }
        return styleNumCenterNoBorder;
    }

    /**
     * 小数 【有边框，左边】
     */
    protected XSSFCellStyle getStyleNumLeftWithBorder() {
        if (styleNumLeftWithBorder == null) {
            styleNumLeftWithBorder = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleNumLeftWithBorder.setAlignment(HorizontalAlignment.LEFT);
            styleNumLeftWithBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            styleNumLeftWithBorder.setDataFormat(getWorkbook().createDataFormat().getFormat("0.00"));
            //边框
            styleNumLeftWithBorder.setBorderBottom(ExcelUtil.BORDER); //下边框
            styleNumLeftWithBorder.setBorderLeft(ExcelUtil.BORDER);//左边框
            styleNumLeftWithBorder.setBorderTop(ExcelUtil.BORDER);//上边框
            styleNumLeftWithBorder.setBorderRight(ExcelUtil.BORDER);//右边框
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleNumLeftWithBorder.setFont(font);
        }
        return styleNumLeftWithBorder;
    }

    /**
     * 小数 【有边框，中间】
     */
    protected XSSFCellStyle getStyleNumCenterWithBorder() {
        if (styleNumCenterWithBorder == null) {
            styleNumCenterWithBorder = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleNumCenterWithBorder.setAlignment(HorizontalAlignment.CENTER);
            styleNumCenterWithBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            styleNumCenterWithBorder.setDataFormat(getWorkbook().createDataFormat().getFormat("0.00"));
            //边框
            styleNumCenterWithBorder.setBorderBottom(ExcelUtil.BORDER); //下边框
            styleNumCenterWithBorder.setBorderLeft(ExcelUtil.BORDER);//左边框
            styleNumCenterWithBorder.setBorderTop(ExcelUtil.BORDER);//上边框
            styleNumCenterWithBorder.setBorderRight(ExcelUtil.BORDER);//右边框
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleNumCenterWithBorder.setFont(font);
        }
        return styleNumCenterWithBorder;
    }


    /*
     * 日期
     */


    /**
     * 日期 【无边框,左边】
     */
    private XSSFCellStyle styleDateLeftNoBorder;
    /**
     * 日期 【无边框，中间】
     */
    private XSSFCellStyle styleDateCenterNoBorder;
    /**
     * 日期 【有边框，左边】
     */
    private XSSFCellStyle styleDateLeftWithBorder;
    /**
     * 日期 【有边框，中间】
     */
    private XSSFCellStyle styleDateCenterWithBorder;


    /**
     * 日期 【无边框,左边】
     */
    protected XSSFCellStyle getStyleDateLeftNoBorder() {
        if (styleDateLeftNoBorder == null) {
            styleDateLeftNoBorder = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleDateLeftNoBorder.setAlignment(HorizontalAlignment.LEFT);
            styleDateLeftNoBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            styleDateLeftNoBorder.setDataFormat(getWorkbook().createDataFormat().getFormat("yyyy-MM-dd"));
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleDateLeftNoBorder.setFont(font);
        }
        return styleDateLeftNoBorder;
    }

    /**
     * 日期 【无边框，中间】
     */
    protected XSSFCellStyle getStyleDateCenterNoBorder() {
        if (styleDateCenterNoBorder == null) {
            styleDateCenterNoBorder = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleDateCenterNoBorder.setAlignment(HorizontalAlignment.CENTER);
            styleDateCenterNoBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            styleDateCenterNoBorder.setDataFormat(getWorkbook().createDataFormat().getFormat("yyyy-MM-dd"));
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleDateCenterNoBorder.setFont(font);
        }
        return styleDateCenterNoBorder;
    }

    /**
     * 日期 【有边框，左边】
     */
    protected XSSFCellStyle getStyleDateLeftWithBorder() {
        if (styleDateLeftWithBorder == null) {
            styleDateLeftWithBorder = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleDateLeftWithBorder.setAlignment(HorizontalAlignment.LEFT);
            styleDateLeftWithBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            styleDateLeftWithBorder.setDataFormat(getWorkbook().createDataFormat().getFormat("yyyy-MM-dd"));
            //边框
            styleDateLeftWithBorder.setBorderBottom(ExcelUtil.BORDER); //下边框
            styleDateLeftWithBorder.setBorderLeft(ExcelUtil.BORDER);//左边框
            styleDateLeftWithBorder.setBorderTop(ExcelUtil.BORDER);//上边框
            styleDateLeftWithBorder.setBorderRight(ExcelUtil.BORDER);//右边框
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleDateLeftWithBorder.setFont(font);
        }
        return styleDateLeftWithBorder;
    }

    /**
     * 日期 【有边框，中间】
     */
    protected XSSFCellStyle getStyleDateCenterWithBorder() {
        if (styleDateCenterWithBorder == null) {
            styleDateCenterWithBorder = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleDateCenterWithBorder.setAlignment(HorizontalAlignment.CENTER);
            styleDateCenterWithBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            styleDateCenterWithBorder.setDataFormat(getWorkbook().createDataFormat().getFormat("yyyy-MM-dd"));
            //边框
            styleDateCenterWithBorder.setBorderBottom(ExcelUtil.BORDER); //下边框
            styleDateCenterWithBorder.setBorderLeft(ExcelUtil.BORDER);//左边框
            styleDateCenterWithBorder.setBorderTop(ExcelUtil.BORDER);//上边框
            styleDateCenterWithBorder.setBorderRight(ExcelUtil.BORDER);//右边框
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleDateCenterWithBorder.setFont(font);
        }
        return styleDateCenterWithBorder;
    }


    /*
     * 日期时间
     */


    /**
     * 日期时间 【无边框,左边】
     */
    private XSSFCellStyle styleDateTimeLeftNoBorder;
    /**
     * 日期时间 【无边框，中间】
     */
    private XSSFCellStyle styleDateTimeCenterNoBorder;
    /**
     * 日期时间 【有边框，左边】
     */
    private XSSFCellStyle styleDateTimeLeftWithBorder;
    /**
     * 日期时间 【有边框，中间】
     */
    private XSSFCellStyle styleDateTimeCenterWithBorder;


    /**
     * 日期时间 【无边框,左边】
     */
    protected XSSFCellStyle getStyleDateTimeLeftNoBorder() {
        if (styleDateTimeLeftNoBorder == null) {
            styleDateTimeLeftNoBorder = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleDateTimeLeftNoBorder.setAlignment(HorizontalAlignment.LEFT);
            styleDateTimeLeftNoBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            styleDateTimeLeftNoBorder.setDataFormat(getWorkbook().createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleDateTimeLeftNoBorder.setFont(font);
        }
        return styleDateTimeLeftNoBorder;
    }

    /**
     * 日期时间 【无边框，中间】
     */
    protected XSSFCellStyle getStyleDateTimeCenterNoBorder() {
        if (styleDateTimeCenterNoBorder == null) {
            styleDateTimeCenterNoBorder = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleDateTimeCenterNoBorder.setAlignment(HorizontalAlignment.CENTER);
            styleDateTimeCenterNoBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            styleDateTimeCenterNoBorder.setDataFormat(getWorkbook().createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleDateTimeCenterNoBorder.setFont(font);
        }
        return styleDateTimeCenterNoBorder;
    }

    /**
     * 日期时间 【有边框，左边】
     */
    protected XSSFCellStyle getStyleDateTimeLeftWithBorder() {
        if (styleDateTimeLeftWithBorder == null) {
            styleDateTimeLeftWithBorder = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleDateTimeLeftWithBorder.setAlignment(HorizontalAlignment.LEFT);
            styleDateTimeLeftWithBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            styleDateTimeLeftWithBorder.setDataFormat(getWorkbook().createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
            //边框
            styleDateTimeLeftWithBorder.setBorderBottom(ExcelUtil.BORDER); //下边框
            styleDateTimeLeftWithBorder.setBorderLeft(ExcelUtil.BORDER);//左边框
            styleDateTimeLeftWithBorder.setBorderTop(ExcelUtil.BORDER);//上边框
            styleDateTimeLeftWithBorder.setBorderRight(ExcelUtil.BORDER);//右边框
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleDateTimeLeftWithBorder.setFont(font);
        }
        return styleDateTimeLeftWithBorder;
    }

    /**
     * 日期时间 【有边框，中间】
     */
    protected XSSFCellStyle getStyleDateTimeCenterWithBorder() {
        if (styleDateTimeCenterWithBorder == null) {
            styleDateTimeCenterWithBorder = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleDateTimeCenterWithBorder.setAlignment(HorizontalAlignment.CENTER);
            styleDateTimeCenterWithBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            styleDateTimeCenterWithBorder.setDataFormat(getWorkbook().createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
            //边框
            styleDateTimeCenterWithBorder.setBorderBottom(ExcelUtil.BORDER); //下边框
            styleDateTimeCenterWithBorder.setBorderLeft(ExcelUtil.BORDER);//左边框
            styleDateTimeCenterWithBorder.setBorderTop(ExcelUtil.BORDER);//上边框
            styleDateTimeCenterWithBorder.setBorderRight(ExcelUtil.BORDER);//右边框
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleDateTimeCenterWithBorder.setFont(font);
        }
        return styleDateTimeCenterWithBorder;
    }


    /*
     * 富文本，自动换行
     */


    /**
     * 富文本，自动换行 【无边框,左边】
     */
    private XSSFCellStyle styleWrapTextLeftNoBorder;
    /**
     * 富文本，自动换行 【无边框，中间】
     */
    private XSSFCellStyle styleWrapTextCenterNoBorder;
    /**
     * 富文本，自动换行 【有边框，左边】
     */
    private XSSFCellStyle styleWrapTextLeftWithBorder;
    /**
     * 富文本，自动换行 【有边框，中间】
     */
    private XSSFCellStyle styleWrapTextCenterWithBorder;


    /**
     * 富文本，自动换行 【无边框,左边】
     */
    protected XSSFCellStyle getStyleWrapTextLeftNoBorder() {
        if (styleWrapTextLeftNoBorder == null) {
            styleWrapTextLeftNoBorder = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleWrapTextLeftNoBorder.setAlignment(HorizontalAlignment.LEFT);
            styleWrapTextLeftNoBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            styleWrapTextLeftNoBorder.setDataFormat(getWorkbook().createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
            styleWrapTextLeftNoBorder.setWrapText(true);
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleWrapTextLeftNoBorder.setFont(font);
        }
        return styleWrapTextLeftNoBorder;
    }

    /**
     * 富文本，自动换行 【无边框，中间】
     */
    protected XSSFCellStyle getStyleWrapTextCenterNoBorder() {
        if (styleWrapTextCenterNoBorder == null) {
            styleWrapTextCenterNoBorder = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleWrapTextCenterNoBorder.setAlignment(HorizontalAlignment.CENTER);
            styleWrapTextCenterNoBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            styleWrapTextCenterNoBorder.setDataFormat(getWorkbook().createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
            styleWrapTextCenterNoBorder.setWrapText(true);
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleWrapTextCenterNoBorder.setFont(font);
        }
        return styleWrapTextCenterNoBorder;
    }

    /**
     * 富文本，自动换行 【有边框，左边】
     */
    protected XSSFCellStyle getStyleWrapTextLeftWithBorder() {
        if (styleWrapTextLeftWithBorder == null) {
            styleWrapTextLeftWithBorder = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleWrapTextLeftWithBorder.setAlignment(HorizontalAlignment.LEFT);
            styleWrapTextLeftWithBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            styleWrapTextLeftWithBorder.setDataFormat(getWorkbook().createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
            styleWrapTextLeftWithBorder.setWrapText(true);
            //边框
            styleWrapTextLeftWithBorder.setBorderBottom(ExcelUtil.BORDER); //下边框
            styleWrapTextLeftWithBorder.setBorderLeft(ExcelUtil.BORDER);//左边框
            styleWrapTextLeftWithBorder.setBorderTop(ExcelUtil.BORDER);//上边框
            styleWrapTextLeftWithBorder.setBorderRight(ExcelUtil.BORDER);//右边框
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleWrapTextLeftWithBorder.setFont(font);
        }
        return styleWrapTextLeftWithBorder;
    }

    /**
     * 富文本，自动换行 【有边框，中间】
     */
    protected XSSFCellStyle getStyleWrapTextCenterWithBorder() {
        if (styleWrapTextCenterWithBorder == null) {
            styleWrapTextCenterWithBorder = (XSSFCellStyle) getWorkbook().createCellStyle();
            styleWrapTextCenterWithBorder.setAlignment(HorizontalAlignment.CENTER);
            styleWrapTextCenterWithBorder.setVerticalAlignment(VerticalAlignment.CENTER);
            styleWrapTextCenterWithBorder.setDataFormat(getWorkbook().createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));
            styleWrapTextCenterWithBorder.setWrapText(true);
            //边框
            styleWrapTextCenterWithBorder.setBorderBottom(ExcelUtil.BORDER); //下边框
            styleWrapTextCenterWithBorder.setBorderLeft(ExcelUtil.BORDER);//左边框
            styleWrapTextCenterWithBorder.setBorderTop(ExcelUtil.BORDER);//上边框
            styleWrapTextCenterWithBorder.setBorderRight(ExcelUtil.BORDER);//右边框
            Font font = getWorkbook().createFont();
            font.setColor(Font.COLOR_NORMAL);
            font.setFontHeightInPoints((short) 10);
            font.setFontName("宋体");
            styleWrapTextCenterWithBorder.setFont(font);
        }
        return styleWrapTextCenterWithBorder;
    }

}
