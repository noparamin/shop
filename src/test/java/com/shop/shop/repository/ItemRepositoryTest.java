package com.shop.shop.repository;

import com.shop.shop.constant.ItemSellStatus;
import com.shop.shop.entity.Item;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.yml")
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;

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
}