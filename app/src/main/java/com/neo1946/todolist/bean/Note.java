package com.neo1946.todolist.bean;

import android.util.Log;
import android.widget.LinearLayout;

import com.neo1946.todolist.util.ImageUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ouyangzhaoxian
 * 笔记实体类
 */

public class Note implements Serializable {

    private int id;//笔记ID
    private String title;//笔记标题
    private String content;//笔记内容
    private int groupId;//分类ID 紧急度
    private String location;//位置
    private int isFinish;//是否完成 1完成 0未完成
    private String bgColor;//背景颜色，存储颜色代码
    private long createTime;//创建时间
    private long updateTime;//修改时间

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getIsFinish() {
        return isFinish;
    }

    public void setIsFinish(int isFinish) {
        this.isFinish = isFinish;
    }

    public String getBgColor() {
        return bgColor;
    }

    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 获取笔记中的图片url
     */
    public List<String> getImageUrls(){
        return getImageUrlsInner(content);
    }

    /**
     * 获取笔记中的网络图片url
     */
    public List<String> getNetworkImageUrls(){
        List<String> list = getImageUrlsInner(content);
        List<String> result = new ArrayList<>();
        for(String url:list){
            if(url.contains("http")){
                result.add(url);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "{\n" +
                "  \"groupId\": "+getGroupId()+",\n" +
                "  \"content\": \""+getContent()+"\",\n" +
                "  \"createTime\": "+getCreateTime()+",\n" +
                "  \"title\": \""+getTitle()+"\",\n" +
                "  \"updateTime\": "+getUpdateTime()+",\n" +
                "  \"isFinish\": "+getIsFinish()+",\n" +
                "  \"id\": "+getId()+",\n" +
                "  \"bgColor\": \""+getBgColor()+"\",\n" +
                "  \"location\": \""+getLocation()+"\",\n" +
                "}";
    }

    private List<String> getImageUrlsInner(String context){
        if (context.contains("<img") && context.contains("src=")) {
            int startIndex = context.indexOf("src=")+5;
            int endIndex = context.indexOf("\"/>");
            List<String> list = getImageUrlsInner(context.substring(endIndex+3,context.length()));
            String imageUrl = context.substring(startIndex,endIndex);
            list.add(imageUrl);
            return list;
        }else{
            return new ArrayList<String>();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Note){
            return (this.getCreateTime() == ((Note) obj).getCreateTime());
        }else {
            return false;
        }
    }
}
