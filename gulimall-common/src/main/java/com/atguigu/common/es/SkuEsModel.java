package com.atguigu.common.es;

import lombok.Data;

import javax.annotation.security.DenyAll;
import java.math.BigDecimal;
import java.util.List;

/**
 * 索引结构
 * PUT product
 * {
 *   "mappings": {
 *     "properties": {
 *       "skuId": {
 *         "type": "long"
 *       },
 *       "spuId": {
 *         "type": "keyword"
 *       },
 *       "skuTitle": {
 *         "type": "text",
 *         "analyzer": "ik_smart"
 *       },
 *       "skuPrice": {
 *         "type": "keyword"
 *       },
 *       "skuImg": {
 *         "type": "keyword",
 *         "index": false,
 *         "doc_values": false
 *       },
 *       "saleCount": {
 *         "type": "long"
 *       },
 *       "hasStock": {
 *         "type": "boolean"
 *       },
 *       "hotScore": {
 *         "type": "long"
 *       },
 *       "brandId": {
 *         "type": "long"
 *       },
 *       "catalogId": {
 *         "type": "long"
 *       },
 *       "brandName": {
 *         "type": "keyword",
 *         "index": false,
 *         "doc_values": false
 *       },
 *       "brandImg": {
 *         "type": "keyword",
 *         "index": false,
 *         "doc_values": false
 *       },
 *       "catalogName": {
 *         "type": "keyword",
 *         "index": false,
 *         "doc_values": false
 *       },
 *       "attrs": {
 *         "type": "nested",
 *         "properties": {
 *           "attrId": {
 *             "type": "long"
 *           },
 *           "attrName": {
 *             "type": "keyword",
 *             "index": false,
 *             "doc_values": false
 *           },
 *           "attrValue": {
 *             "type": "keyword"
 *           }
 *         }
 *       }
 *     }
 *   }
 * }
 */
@Data
public class SkuEsModel {
    /**
     * skuId
     */
    private Long skuId;

    /**
     * spuId
     */
    private Long spuId;

    /**
     * sku标题
     */
    private String skuTitle;

    /**
     * sku价格
     */
    private BigDecimal skuPrice;

    /**
     * sku图片
     */
    private String skuImg;

    /**
     * 销售数量
     */
    private Long saleCount;

    /**
     * 是否售空
     */
    private Boolean hasStock;

    /**
     * 热度评分
     */
    private Long hotScore;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 目录ID
     */
    private Long catalogId;

    /**
     * 品牌名
     */
    private String brandName;

    /**
     * 品牌图片
     */
    private String brandImg;

    /**
     * 目录名
     */
    private String catalogName;

    /**
     * 属性对象集合
     */
    private List<Attrs> attrs;

    @Data
    public static class Attrs {

        private Long attrId;

        private String attrName;

        private String attrValue;

    }
}
