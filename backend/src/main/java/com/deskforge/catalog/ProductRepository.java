package com.deskforge.catalog;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Override
    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Specification<Product> specification, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "specs"})
    Optional<Product> findBySlugAndActiveTrue(String slug);

    @EntityGraph(attributePaths = "category")
    List<Product> findTop8ByFeaturedTrueAndActiveTrueOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "category")
    List<Product> findTop4ByCategoryIdAndIdNotAndActiveTrue(Long categoryId, Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id in :ids order by p.id")
    List<Product> findAllByIdForUpdate(@Param("ids") Collection<Long> ids);

    @EntityGraph(attributePaths = "category")
    List<Product> findTop5ByActiveTrueAndStockLessThanEqualOrderByStockAsc(int threshold);

    boolean existsBySku(String sku);

    boolean existsBySlug(String slug);
}
