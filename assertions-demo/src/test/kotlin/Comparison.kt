import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasElement
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

class Comparison {
    @TestFactory
    fun lists(): List<DynamicTest> {
        val expectedList = listOf("Alan", "Mary", "Joe")
        val actualList = listOf("Alan", "Paige", "Joe")

        data class TestCase(val description: String, val allowToThrow: Boolean = false, val block: () -> Unit)

        val testCases = listOf(
            TestCase("hamkrest - equals") {
                // my immediate issue here is discoverability. where the heck do I even find the matchers, without already knowing what I'm looking for?
                // I had to go and look at the library sources, found a file called `core_matchers` and was able to find an assertion in there.
                // even when I look at IDE suggestions, equalTo doesn't show up.
                // Hamkrest also doesn't give a diff

                assertThat(actualList, equalTo(expectedList))

                /**
                 * java.lang.AssertionError: expected: a value that is equal to ["Alan", "Mary", "Joe"]
                 * but was: ["Alan", "Paige", "Joe"]
                 */
            },
            TestCase("hamkrest - equals isn't typesafe") {
                // it's not typesafe

                assertThat(actualList, equalTo(123))

                /**
                 * java.lang.AssertionError: expected: a value that is equal to 123
                 * but was: ["Alan", "Paige", "Joe"]
                 */
            },
            TestCase("hamkrest - contains") {
                // again, I had to look at the library code to find this method

                assertThat(actualList, hasElement("Benny"))

                /**
                 * java.lang.AssertionError: expected: a value that contains "Benny"
                 * but was ["Alan", "Paige", "Joe"]
                 */
            },
            TestCase("kotest - equals") {
                // kotest uses extension functions for easy discoverability

                actualList shouldBe expectedList

                /**
                 * Element differ at index: [1]
                 * expected:<["Alan", "Mary", "Joe"]> but was:<["Alan", "Paige", "Joe"]>
                 * Expected :["Alan", "Mary", "Joe"]
                 * Actual   :["Alan", "Paige", "Joe"]
                 * <Click to see difference>
                 */
            },
            TestCase("kotest - equals isn't typesafe") {
                // However... it's not typesafe...

                actualList shouldBe 123

                /**
                 * expected:kotlin.Int<123> but was:java.util.Arrays.ArrayList<["Alan", "Paige", "Joe"]>
                 * Expected :123
                 * Actual   :["Alan", "Paige", "Joe"]
                 * <Click to see difference>
                 */
            },
            TestCase("kotest - contains") {
                actualList shouldContain "Benny"

                /**
                 * Collection should contain element "Benny" based on object equality; but the collection is ["Alan", "Paige", "Joe"]
                 */
            },
            TestCase("strikt - equals") {
                // strikt uses extension functions for easy discoverability

                expectThat(actualList) isEqualTo expectedList

                /**
                 * ▼ Expect that ["Alan", "Paige", "Joe"]:
                 *   ✗ is equal to ["Alan", "Mary", "Joe"]
                 *           found ["Alan", "Paige", "Joe"]
                 * Expected :[Alan, Mary, Joe]
                 * Actual   :[Alan, Paige, Joe]
                 * <Click to see difference>
                 */
            },
            // this doesn't compile because it's typesafe
            //TestCase("strikt - equals is typesafe") {
            //    expectThat(actualList) isEqualTo 123
            //},
            TestCase("strikt - contains") {
                expectThat(actualList).contains("Benny")
                /**
                 * ▼ Expect that ["Alan", "Paige", "Joe"]:
                 *   ✗ contains "Benny"
                 */
            },
            TestCase("strikt - contains isn't typesafe") {
                // contains is not typesafe, for now - https://github.com/robfletcher/strikt/issues/137

                expectThat(actualList).contains(123)

                /**
                 * ▼ Expect that ["Alan", "Paige", "Joe"]:
                 *   ✗ contains 123
                 */
            },
        )

        return testCases.map {
            DynamicTest.dynamicTest(it.description) {
                when (val exception = runCatching(it.block).exceptionOrNull()) {
                    is AssertionError -> if (it.allowToThrow) throw exception else println(exception.stackTraceToString())
                    null -> error("expected an assertion error")
                    else -> throw exception
                }
            }
        }
    }
}
