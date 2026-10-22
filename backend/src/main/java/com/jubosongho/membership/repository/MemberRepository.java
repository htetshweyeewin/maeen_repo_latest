package com.jubosongho.membership.repository;

import com.jubosongho.membership.domain.Member;
import com.jubosongho.membership.domain.MemberStatus;
import com.jubosongho.membership.domain.OrgZila;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface MemberRepository extends JpaRepository<Member, Long>, JpaSpecificationExecutor<Member> {

    boolean existsByMobile1AndStatusNot(String mobile1, MemberStatus status);

    boolean existsByMobile1AndStatusNotAndIdNot(String mobile1, MemberStatus status, Long id);

    long countByOrgZila(OrgZila orgZila);

    @Modifying
    @Query("update Member m set m.orgZila = null where m.orgZilaManual = false and m.orgZila = :zila")
    int detachAutoAssigned(@Param("zila") OrgZila zila);

    @Modifying
    @Query("update Member m set m.orgZila = :zila where m.orgZilaManual = false "
            + "and m.presentAddress.upazilaId in :upazilaIds")
    int attachByUpazila(@Param("zila") OrgZila zila, @Param("upazilaIds") Collection<Long> upazilaIds);

    @Modifying
    @Query("update Member m set m.orgZila = null, m.orgZilaManual = false where m.orgZila = :zila")
    int detachAll(@Param("zila") OrgZila zila);
}
