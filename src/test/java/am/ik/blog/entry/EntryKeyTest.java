package am.ik.blog.entry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntryKeyTest {

	@Test
	void constructorWithEntryIdOnly() {
		EntryKey key = new EntryKey(123L, null);
		assertThat(key.entryId()).isEqualTo(123L);
		assertThat(key.tenantId()).isNull();
	}

	@Test
	void constructorWithEntryIdAndTenantId() {
		EntryKey key = new EntryKey(456L, "tenant1");
		assertThat(key.entryId()).isEqualTo(456L);
		assertThat(key.tenantId()).isEqualTo("tenant1");
	}

	@Test
	void toStringWithoutTenantId() {
		EntryKey key = new EntryKey(123L, null);
		assertThat(key.toString()).isEqualTo("00123");
	}

	@Test
	void toStringWithTenantId() {
		EntryKey key = new EntryKey(456L, "tenant1");
		assertThat(key.toString()).isEqualTo("00456|tenant1");
	}

	@Test
	void toStringWithTenantIdContainingPipe() {
		EntryKey key = new EntryKey(100L, "tenant|with|pipe");
		assertThat(key.toString()).isEqualTo("00100|\"tenant|with|pipe\"");
	}

	@ParameterizedTest
	@CsvSource({ "00123, 123, ", "00456|tenant1, 456, tenant1", "00789|tenant2, 789, tenant2",
			"100|\"tenant|with|pipe\", 100, tenant|with|pipe" })
	void valueOf(String value, Long expectedEntryId, String expectedTenantId) {
		EntryKey key = EntryKey.valueOf(value);
		assertThat(key.entryId()).isEqualTo(expectedEntryId);
		assertThat(key.tenantId()).isEqualTo(expectedTenantId);
	}

	@Test
	void valueOfWithInvalidFormat() {
		assertThatThrownBy(() -> EntryKey.valueOf("00123|tenant1|extra")).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid EntryKey format: 00123|tenant1|extra");
	}

	@Test
	void valueOfWithEmptyString() {
		assertThatThrownBy(() -> EntryKey.valueOf("")).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid EntryKey format: ");
	}

	@Test
	void valueOfWithInvalidEntryId() {
		assertThatThrownBy(() -> EntryKey.valueOf("invalid")).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid EntryKey format: invalid")
			.hasCauseInstanceOf(NumberFormatException.class);
	}

	@Test
	void equalsAndHashCode() {
		EntryKey key1 = new EntryKey(123L, "tenant1");
		EntryKey key2 = new EntryKey(123L, "tenant1");
		EntryKey key3 = new EntryKey(456L, "tenant1");
		EntryKey key4 = new EntryKey(123L, "tenant2");
		EntryKey key5 = new EntryKey(123L, null);

		assertThat(key1).isEqualTo(key2);
		assertThat(key1.hashCode()).isEqualTo(key2.hashCode());

		assertThat(key1).isNotEqualTo(key3);
		assertThat(key1).isNotEqualTo(key4);
		assertThat(key1).isNotEqualTo(key5);
	}

	@Test
	void roundTripConversion() {
		EntryKey original = new EntryKey(123L, "tenant1");
		String stringValue = original.toString();
		EntryKey reconstructed = EntryKey.valueOf(stringValue);
		assertThat(reconstructed).isEqualTo(original);
	}

	@Test
	void roundTripConversionWithoutTenantId() {
		EntryKey original = new EntryKey(456L, null);
		String stringValue = original.toString();
		EntryKey reconstructed = EntryKey.valueOf(stringValue);
		assertThat(reconstructed).isEqualTo(original);
	}

	@Test
	void roundTripConversionWithTenantIdContainingPipe() {
		EntryKey original = new EntryKey(100L, "tenant|with|pipe");
		String stringValue = original.toString();
		EntryKey reconstructed = EntryKey.valueOf(stringValue);
		assertThat(reconstructed).isEqualTo(original);
	}

}