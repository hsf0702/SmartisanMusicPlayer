package com.yibao.music.util;

import com.yibao.music.base.listener.LyricDownCallBack;
import com.yibao.music.base.listener.LyricsCallBack;
import com.yibao.music.model.LyricDownBean;
import com.yibao.music.model.MusicBean;
import com.yibao.music.model.MusicLyricBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @项目名： BigGirl
 * @包名： com.yibao.biggirl.util
 * @文件名: LyricsUtil
 * @author: Stran
 * @Email: www.strangermy98@gmail.com
 * @创建时间: 2018/1/28 22:08
 * @描述： {将歌词封装到List里}
 */

public class LyricsUtil {

    private static BufferedReader br;
    private static final String UNKNOWN_NAME = "<unknown>";

    public static boolean checkLyricFile(String songName, String songArtisa) {
        LogUtil.d(" 本地歌词信息  " + songName + " $$ " + songArtisa);
        String path = Constants.MUSIC_LYRICS_ROOT + songName + "$$" + songArtisa + ".lrc";
        File file = new File(path);
        return file.exists();
    }


    /**
     * 将歌词封装到list中
     */
    public static void downloadLyricFile(MusicBean musicBean) {
        String songName = StringUtil.getTitle(musicBean);
        String artist = StringUtil.getArtist(musicBean);
        if (NetworkUtil.isNetworkConnected()) {

            // 先获取网络歌词的下载地址Url,，如果没有歌词地址或者地址下载失败，返回" 暂无歌词"

            DownloadLyricsUtil.downloadLyricUrl(musicBean, new LyricsCallBack() {
                @Override
                public void lyricsUri(boolean lyricsUrlOk, String lyricsUri) {
                    LogUtil.d("====== downloadLyricUrl  =====       " + lyricsUrlOk);
                    if (lyricsUrlOk && lyricsUri != null) {
                        // 发现歌词下载地址，下载歌词。
                        DownloadLyricsUtil.downloadlyricsfile(lyricsUri, songName, artist);
                    } else {
                        LyricDownBean lyricDownBean = new LyricDownBean(false, null, Constants.NO_FIND_LYRICS);
                        RxBus.getInstance().post(Constants.MUSIC_LYRIC_OK, lyricDownBean);
                    }

                }
            });
        } else {
            LyricDownBean lyricDownBean = new LyricDownBean(false, null, Constants.NO_FIND_NETWORK);
            RxBus.getInstance().post(Constants.MUSIC_LYRIC_OK, lyricDownBean);
        }
    }

    /**
     * 将读取本地歌词文件，将歌词封装到List中。
     *
     * @return 返回歌词List
     */
    public static List<MusicLyricBean> getLyricList(File file) {
        List<MusicLyricBean> lrcList = new ArrayList<>();
        if (!file.exists()) {
            lrcList.add(new MusicLyricBean(0, "没有发现歌词"));
        } else {
            ThreadPoolProxyFactory.newInstance().execute(() -> {
                try {
                    String charsetName = "utf-8";
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charsetName));
                    String line = br.readLine();
                    while (line != null) {
                        ArrayList<MusicLyricBean> been = parseLine(line);
                        lrcList.addAll(been);
                        if (br != null) {
                            line = br.readLine();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    lrcList.add(new MusicLyricBean(0, "歌词加载出错"));
                } finally {
                    try {
                        if (br != null) {
                            br.close();
                            br = null;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            });

        }
        Collections.sort(lrcList);
        return lrcList;
    }


    private static ArrayList<MusicLyricBean> parseLine(String str) {
        ArrayList<MusicLyricBean> list = new ArrayList<>();
        String[] arr = str.split("]");
        String content = arr[arr.length - 1];

        for (int i = 0; i < arr.length - 1; i++) {
            if (arr.length > i) {
                int startTime = parseTime(arr[i]);
                MusicLyricBean lrcBean = new MusicLyricBean(startTime, content);
                list.add(lrcBean);
            }
        }
        return list;
    }

    /**
     * 对歌词时间可能出现汉字或英文字母做了处理
     *
     * @param s s
     * @return d
     */
    private static int parseTime(String s) {
        int mtime = 0;
        boolean containsChinese = isContainsEnglishAndChinese(s);
        String braces = "[";
        if (containsChinese) {
            LogUtil.d("============== 歌词时间解析异常！================");
            return 0;
        } else {
            String[] arr = s.split(":");
            String min = arr[0].substring(1);
            if (min.contains(braces)) {
                min = "00";
            }
            if (arr.length > 1) {
                String sec = arr[1];
                boolean b = isContainsEnglishAndChinese(sec);
                mtime = (int) (Integer.parseInt(min) * 60 * 1000 + Float.parseFloat(b ? "1" : sec) * 1000);
            }
            return arr.length > 1 ? mtime : 1;
        }

    }

    private static boolean isContainsEnglishAndChinese(String str) {
        String regex = "^[a-z0-9A-Z\\\\u4e00-\u9fa5]+$";
        return str.matches(regex);
    }
}