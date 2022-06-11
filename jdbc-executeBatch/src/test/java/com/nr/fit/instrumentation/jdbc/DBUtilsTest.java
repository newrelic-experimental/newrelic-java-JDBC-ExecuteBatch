package com.nr.fit.instrumentation.jdbc;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DBUtilsTest {

    @Test
    void testCollectionAndOperation1() {
        assertThat(DBUtils.parse("SELECT * FROM blob"))
                .extracting(CollectionAndOperation::getCollection, CollectionAndOperation::getOperation)
                .containsExactly("blob", "select");
    }

    @Test
    void testCollectionAndOperation2() {
        assertThat(DBUtils.parse("WITH x(), y()SELECT * FROM blob"))
                .extracting(CollectionAndOperation::getCollection, CollectionAndOperation::getOperation)
                .containsExactly("blob", "select");
    }

    @Test
    void testCollectionAndOperation_comment() {
        assertThat(DBUtils.parse(
                "/* Upsert contact */\n" +
                        "INSERT INTO contact(id) values(?)"))
                .extracting(CollectionAndOperation::getCollection, CollectionAndOperation::getOperation)
                .containsExactly("Upsert contact", "batch");
    }

    @Test
    void testCollectionAndOperation3() {
        assertThat(DBUtils.parse("SELECT 1, a (SELECT a FROM b) FROM blob ORDER BY frog"))
                .extracting(CollectionAndOperation::getCollection, CollectionAndOperation::getOperation)
                .containsExactly("blob", "select");
    }

    @Test
    void testCollectionAndOperation4() {
        assertThat(DBUtils.parse("WITH tombstone AS (\n"
                + "    INSERT INTO contact_tombstone(contact_id)\n"
                + "    VALUES (:id)\n"
                + "    ON CONFLICT DO NOTHING\n"
                + ")\n"
                + "DELETE FROM contact WHERE id = :id"))
                .extracting(CollectionAndOperation::getCollection, CollectionAndOperation::getOperation)
                .containsExactly("contact", "delete");
    }

    @Test
    void testEmptyParentheticals1() {
        assertThat(DBUtils.emptyParentheticals("SELECT * FROM blob")).isEqualTo("SELECT * FROM blob");
    }

    @Test
    void testEmptyParentheticals2() {
        assertThat(DBUtils.emptyParentheticals("SELECT a, (select 1) FROM blob"))
                .isEqualTo("SELECT a, () FROM blob");
    }

    @Test
    void testEmptyParentheticals3() {
        assertThat(DBUtils.emptyParentheticals("WITH tombstone AS (\n"
                + "    INSERT INTO contact_tombstone(contact_id)\n"
                + "    VALUES (:id)\n"
                + "    ON CONFLICT DO NOTHING\n"
                + ")\n"
                + "DELETE FROM contact WHERE id = :id"))
                .isEqualTo("WITH tombstone AS ()\n"
                        + "DELETE FROM contact WHERE id = :id");
    }

    @Test
    void testEmptyParentheticals4() {
        assertThat(DBUtils.emptyParentheticals("WITH x(), y()SELECT * FROM blob"))
                .isEqualTo("WITH x(), y()SELECT * FROM blob");
    }
}
