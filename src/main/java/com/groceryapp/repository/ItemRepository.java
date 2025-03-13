package com.groceryapp.repository;

import com.groceryapp.model.Brand;
import com.groceryapp.model.Category;
import com.groceryapp.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByCategoryAndBrand(Category category, Brand brand);
}