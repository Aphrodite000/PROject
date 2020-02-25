package util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PinyinUtil {//第一大步
    /**
     * 中文字符格式
     */
    private static final String CHINESE_PATTERN = "[\\u4E00-\\u9FA5]";
    /**
     * 汉语拼音格式化
     */
    private static final HanyuPinyinOutputFormat FORMAT=new HanyuPinyinOutputFormat();
    static {
        //格式化类初始化
        FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        //拼音小写
        FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        //带音调
        FORMAT.setVCharType(HanyuPinyinVCharType.WITH_V);
        //设置带v，如绿 lv
    }
    //文件名是否包含中文，字符串是否包含文件
    public static boolean containsChinese(String name){
        //String中matches方法，用来匹配  matches(.)匹配任意字符   matches(a[bc])匹配ab或者ac   []是任意一个的意思
        //name.matches(CHINESE_PATTERN );一个字符匹配的
        return name.matches(".*"+CHINESE_PATTERN +".*");//前后加上.就全部匹配，中间任意字符串匹配上
        //前后都是任意字符，任意字符可以出现0次和多次，中间只要有任意一个字符匹配我的中文就行
    }


    /**  第一步.
     * 通过文件名获取全拼+拼音首字母
     * 中华人民共和国--->zhonghuarenmingongheguo/zhrmghg
     * @param name 文件名
     * @return 两个字符串 返回一个数组
     */
    //项目只要这个方法
    public static String[] get(String name){
        //返回的是一个字符串数组，那就先定义一个字符串数字
        String[] result=new String[2];
        //定义一个拼接字符串对象
        StringBuilder pinyin=new StringBuilder();//线程不安全，局部变量（引用类型的局部变量)，所以安不安全没有关系
        StringBuilder pinyinFirst=new StringBuilder();
        //获取中 和  遍历文件名字符
        for(char c:name.toCharArray()){
            //[zhong] [he,hu,huo]
            try {
                //字符和格式化对象,字符是string分离来的   此处会有解析异常，要处理
                String[] pinyins=PinyinHelper.toHanyuPinyinStringArray(c,FORMAT);//这个是解析中文变成拼音
                //如果是日文韩文，如果出错了，就返回原始字符   异常处理 类似这样
                if(pinyins==null||pinyins.length==0){ //如果中间有空格，是报异常还是不报异常，走 if
                    //解析工具没有解析到，就添加原始字符
                    pinyin.append(c);
                    pinyinFirst.append(c);
                }else{
                    //不等于零说明有返回值，取第一拼音
                    pinyin.append(pinyins[0]);//直接取he
                    pinyinFirst.append(pinyins[0].charAt(0));//取he中的h
                }
            } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                pinyin.append(c);
                pinyinFirst.append(c);
                //问题：既然这里会捕捉异常，那为啥上面还要写一遍，直接在异常这里处理就好啊
            }
        }
        result[0]=pinyin.toString();
        result[1]=pinyinFirst.toString();
        return result;
        //返回每个中文字符的拼音只有一个，他们拼接成返回的字符串
    }

    /**  第二步.
     *加入一个文件名交 和长  和[he hu huo ] 长[zhang chang]又加一个和  就有18种类
     * @param name 文件中文名
     * @param fullSpell true表示全拼，false取拼音首字母
     * @return 包含多音字的字符串组合，相当于返回字符串二维数组
     * [ [he hu huo ] ,[zhang chang] ,[he hu huo ] ]   全拼全取，首字母取每个解析出来的元素的首字母就行
     */
    //改良后得到已去重大的二维数组
    public static String[][] get(String name,boolean fullSpell){
        char[] chars=name.toCharArray();//中文
        String[][] result=new String[chars.length][];
        for(int i=0;i<chars.length;i++){
            try {
                //去除音调了 “只” 返回zhi 三声 zhi一声
                String[] pinyins=PinyinHelper.toHanyuPinyinStringArray(chars[i],FORMAT);
                if(pinyins==null||pinyins.length==0){
                    //新建一个字符串数组这个字符串数组只存一个元素，
                    // 也就是新建一行，每一行是一维，行里面还有多个，所以是二维
                    result[i]=new String[]{String.valueOf(chars[i])};
                }else{
                    //原方法 判断全拼，全取result=pinyins；首字母 遍历去首字母形成新数组，result=array;
                    //改良：去重
                    result[i]=unique(pinyins,fullSpell);
                }
            } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                result[i]=new String[]{String.valueOf(chars[i])};//把原始字符放到字符串数组里，只有他这一个元素
            }
        }
        return result;
        //返回每个中文字符的拼音是字符串数组
    }

    /** 第四步
     * 对于第二步的去重   字符串数组去重  然后修改第二步
     * @param array
     * @return
     */
    public static String[] unique(String[] array,boolean fullSpell){
        Set<String> set=new HashSet<>();
        for(String s:array){
            if(fullSpell){
                set.add(s);
            }else{
                set.add(String.valueOf(s.charAt(0)));
            }
        }
        //set转数组
        return set.toArray(new String[set.size()]);
    }
    /** 第三步.     三.2
     * he hu huo    zhang chang
     * hezhang hechang huzhang huchang huochang huozhang
     *每个中文字符返回的拼音是字符串数组，每两个字符串数组合并成为一个字符串数组，以此类推
     *
     * 第一个方法是解析到拼音，和组合拼音一步走
     * 第二个方法是只解析拼音（因为一个汉字字符会解析成多个拼音）
     * 第三个方法是拼接拼音
     */

    //结果返回全部拼接完成的一个字符串数组   两个合成一个，再跟第三个一起合并成一个.....
    public static String[] compose(String[][] pinyinArray){
        if(pinyinArray==null||pinyinArray.length==0){
            return null;
        }else if(pinyinArray.length==1){
            return pinyinArray[0];
        }else{
            //始终把第一个字符串数组当成结果数组  a[0]与a[1]组合放到a[0],a[0]与a[2]组合放到a[0]......7
            //注意i等于1,0已经出现了，从1开始
            for(int i=1;i<pinyinArray.length;i++){
                pinyinArray[0]=compose(pinyinArray[0],pinyinArray[i]);
            }
            return pinyinArray[0];
        }
    }

    /** 第三步    三.1
     * 两个字符串数组合并成一个的结果
     * @param pinyinsl 第一个  he hu huo
     * @param pinyins2 第二个 zhang chang
     * @return   hezhang hechang huzhang huchang huochang huozhang
     */
    public static String[] compose(String[] pinyinsl,String[] pinyins2){
        String[] result=new String[pinyinsl.length*pinyins2.length];
        for(int i=0;i<pinyinsl.length;i++){
            for(int j=0;j<pinyins2.length;j++){
                result[i* pinyins2.length+j]=pinyinsl[i]+pinyins2[j];
            }
        }
        return result;
        /**
         * String[] result=new String[pinyinsl.length*pinyins2.length];
         *         int i=0;
         *         for(String p1:pinyinsl){
         *             for(String p2:pinyins2){
         *                 result[i]=p1+p2;
         *                 i++;
         *             }
         *         }
         *         return result;
         */

    }
    public static void main(String[] args) {
        System.out.println(Arrays.toString(get("中华人民共和国")));
        System.out.println(Arrays.toString(get("中华1人A民b共和国")));

        System.out.println(Arrays.toString(compose(get("和长和",false))));
    }
}
