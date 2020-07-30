package com.moon.core.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.Labels;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.moon.core.lang.StringUtil;
import com.moon.core.util.TestUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author moonsky
 */
class TreeElementTestTest {

    @Test
    void testBase64() throws Exception {
        byte[] bytes = Base64.getDecoder().decode("5pys5pyI5qaC5Ya1");
        System.out.println(new String(bytes));
        bytes = Base64.getUrlDecoder().decode("5LiK5rW35Zu96ZmF5LyB5Lia5ZWG5Yqh5ZKo6K-i5pyN5Yqh5pyJ6ZmQ5YWs5Y-477yI5Yy65Z-f77yJ");
        System.out.println(new String(bytes));
    }

    @Test
    @Disabled
    void testOfArray() throws Exception {
        List<KeyValue> list = getCitiesList();
        /**
         * 树化
         */
        List<TreeElement<KeyValue>> elements = TreeElement.fromList(list, kv -> {
            String key = kv.getKey();
            if (key.endsWith("0000")) {
                return null;
            }
            if (key.endsWith("00")) {
                return key.substring(0, 2) + "0000";
            }
            return key.substring(0, 4) + "00";
        }, KeyValue::getKey, KeyValue::getValue, key -> {
            if (key.endsWith("00")) {
                return key.substring(0, 2) + "0000";
            } else {
                return key.substring(0, 4) + "00";
            }
        });
        System.out.println(elements);

        SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
        filter.getExcludes().add("data");
        System.out.println(JSON.toJSONString(elements, Labels.excludes("data")));
        System.out.println("========================================");
        System.out.println(JSON.toJSONString(elements, filter));
    }

    /**
     * 获取所有区级行政区代码
     *
     * @return
     *
     * @throws IOException
     */
    private List<KeyValue> getCitiesList() throws IOException {
        Document document = Jsoup.connect("http://www.mca.gov.cn///article/sj/xzqh/2020/2020/2020072805001.html").get();
        List<Element> list = document.body().select("td").stream().filter(element -> {
            return StringUtil.isNotBlank(element.text());
        }).collect(Collectors.toList());
        Iterator<Element> iterator = list.iterator();
        List<KeyValue> cities = new ArrayList<>();
        while (true) {
            String cityCodeVal = null;
            while (iterator.hasNext()) {
                Element element = iterator.next();
                String text = element.text().trim();
                if (text.length() == 6 && TestUtil.isDigit(text)) {
                    cityCodeVal = text;
                    break;
                }
            }
            if (iterator.hasNext()) {
                String cityNameVal = iterator.next().text();
                if (StringUtil.isNotEmpty(cityCodeVal) && StringUtil.isNotEmpty(cityNameVal)) {
                    cities.add(KeyValue.of(cityCodeVal, cityNameVal));
                }
            } else {
                break;
            }
        }
        return cities;
    }
}