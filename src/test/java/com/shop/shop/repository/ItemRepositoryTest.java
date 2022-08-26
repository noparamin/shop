package com.shop.shop.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.shop.constant.ItemSellStatus;
import com.shop.shop.entity.Item;
import com.shop.shop.entity.QItem;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.yml")
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    @DisplayName("상품 저장 테스트")
    public void createItemTest() {
        // given
        Item item = new Item();
        item.setItemName("테스트 상품");
        item.setPrice(1000);
        item.setItemDetail("테스트 상품 설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        item.setRegTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());

        // when
        Item savedItem = itemRepository.save(item);
        Long id = savedItem.getId();
        Optional<Item> findItem = itemRepository.findById(id);

        // then
        Assertions.assertThat(savedItem).isEqualTo(item);
    }

    public void createItemList() {
        for(int i=1; i<=10; i++) {
            Item item = new Item();
            item.setItemName("테스트 상품" + i);
            item.setPrice(1000 + i);
            item.setItemDetail("테스트 상품 설명" + i);
            item.setItemSellStatus(ItemSellStatus.SELL);
            item.setStockNumber(100);
            item.setRegTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());

            itemRepository.save(item);
        }
    }

    @Test
    @DisplayName("상품명 조회 테스트")
    public void findByItemNameTest() {
        this.createItemList();
        List<Item> itemList = itemRepository.findByItemName("테스트 상품1");
        Assertions.assertThat(itemList.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("상품명 or 상품상세설명 조회 테스트")
    public void findByItemNameOrItemDetailTest() {
        this.createItemList();
        List<Item> itemList = itemRepository.findByItemNameOrItemDetail("테스트 상품1", "테스트 상품 설명5");
        Assertions.assertThat(itemList.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("특정 가격미만인 상품 조회 테스트")
    public void findByPriceLessThanTest() {
        this.createItemList();
        List<Item> itemList = itemRepository.findByPriceLessThan(1005);
        Assertions.assertThat(itemList.size()).isEqualTo(4);
    }

    @Test
    @DisplayName("특정 가격미만인 상품 내림차순 조회 테스트")
    public void findByPriceLessThanOrderByPriceDescTest() {
        this.createItemList();
        List<Item> itemList = itemRepository.findByPriceLessThanOrderByPriceDesc(1005);
        Assertions.assertThat(itemList.size()).isEqualTo(4);
        int price = 1004;
        for (Item item : itemList) {
            Assertions.assertThat(item.getPrice()).isEqualTo(price);
            price -= 1;
        }
    }

    @Test
    @DisplayName("상품 상세설명을 조건으로 하는 Query를 이용한 상품 조회 테스트")
    public void findByItemDetailTest() {
        // given
        this.createItemList();

        // when
        List<Item> itemList = itemRepository.findByItemDetail("테스트 상품 설명");

        // then
        Assertions.assertThat(itemList.size()).isEqualTo(10);
        int price = 1010;
        for(Item item : itemList) {
            Assertions.assertThat(item.getPrice()).isEqualTo(price);
            price -= 1;
        }
    }

    @Test
    @DisplayName("QueryDsl 조회 테스트")
    public void queryDslTest() {
        // given
        this.createItemList();

        // whem
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QItem qItem = QItem.item;
        JPAQuery<Item> query = queryFactory.selectFrom(qItem)
                .where(qItem.itemSellStatus.eq(ItemSellStatus.SELL))
                .where(qItem.itemDetail.like("%" + "테스트 상품 설명" + "%"))
                .orderBy(qItem.price.desc());

        List<Item> itemList = query.fetch();

        // then
        Assertions.assertThat(itemList.size()).isEqualTo(10);
        int price = 1010;
        for(Item item : itemList) {
            Assertions.assertThat(item.getPrice()).isEqualTo(price);
            price -= 1;
        }
    }

    public void createItemList2() {
        for(int i=1; i<=5; i++) {
            Item item = new Item();
            item.setItemName("테스트 상품" + i);
            item.setPrice(1000 + i);
            item.setItemDetail("테스트 상품 설명" + i);
            item.setItemSellStatus(ItemSellStatus.SELL);
            item.setStockNumber(100);
            item.setRegTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            itemRepository.save(item);
        }

        for(int i=6; i<=10; i++) {
            Item item = new Item();
            item.setItemName("테스트 상품" + i);
            item.setPrice(1000 + i);
            item.setItemDetail("테스트 상품 설명" + i);
            item.setItemSellStatus(ItemSellStatus.SOLD_OUT);
            item.setStockNumber(0);
            item.setRegTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            itemRepository.save(item);
        }
    }

    @Test
    @DisplayName("상품 QueryDsl 조회 테스트2")
    public void queryDslTest2() {
        this.createItemList2();

        BooleanBuilder booleanBuilder = new BooleanBuilder();
        QItem item = QItem.item;
        String itemDetail ="테스트 상품 설명";
        int price = 1003;
        String itemSellStat = "SELL";

        booleanBuilder.and(item.itemDetail.like("%" + itemDetail + "%"));
        booleanBuilder.and(item.price.gt(price));

        if(StringUtils.equals(itemSellStat, ItemSellStatus.SELL)) {
            booleanBuilder.and(item.itemSellStatus.eq(ItemSellStatus.SELL));
        }

        Pageable pageable = PageRequest.of(0, 5);
        Page<Item> itemPagingResult = itemRepository.findAll(booleanBuilder, pageable);

        List<Item> resultItemList = itemPagingResult.getContent();
        Assertions.assertThat(resultItemList.size()).isEqualTo(2);
        for(Item resultItem : resultItemList) {
            System.out.println(resultItem.toString());
        }
    }


}