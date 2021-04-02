package com.github.jameshnsears.quoteunquote.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationHelperTest {
    private val notificationHelper = NotificationHelper()

    private fun largeMultiLineQuotation() =
        """
            To be, or not to be: that is the question:
            Whether ‘tis nobler in the mind to suffer
            The slings and arrows of outrageous fortune,
            Or to take arms against a sea of troubles,
            And by opposing end them? To die: to sleep;
            No more; and, by a sleep to say we end
            The heartache and the thousand natural shocks
            That flesh is heir to, ‘tis a consummation
            Devoutly to be wish’d. To die, to sleep;
            To sleep: perchance to dream: ay, there’s the rub;
            For in that sleep of death what dreams may come
            When we have shuffled off this mortal coil,
            Must give us pause. There’s the respect
            That makes calamity of so long life;
            For who would bear the whips and scorns of time,
            The oppressor’s wrong, the proud man’s contumely,
            The pangs of dispriz’d love, the law’s delay,
            The insolence of office, and the spurns
            That patient merit of the unworthy takes,
            When he himself might his quietus make
            With a bare bodkin? who would fardels bear,
            To grunt and sweat under a weary life,
            But that the dread of something after death,
            The undiscover’d country from whose bourn No traveler returns, puzzles the will,
            And makes us rather bear those ills we have
            Than fly to others that we know not of?
            Thus conscience does make cowards of us all;
            And thus the native hue of resolution Is sicklied o’er with the pale cast of thought,
            And enterprises of great pith and moment
            With this regard their currents turn awry,
            And lose the name of action. III, i, 56
        """.trimIndent()

    private fun largeMultiLineQuoationReduced() =
        """
            To be, or not to be: that is the question:
            Whether ‘tis nobler in the mind to suffer
            The slings and arrows of outrageous fortune,
            Or to take arms against a sea of troubles,
            And by ...
        """.trimIndent()

    private fun largeSingleLineQuotation() =
        """
            The legitimate object of government, is to do for a community of people, whatever they need to have done, but can not do, at all, or can not, so well do, for themselves in their separate, and individual capacities. In all that the people can individually do as well for themselves, government ought not to interfere. The desirable things which the individuals of a people can not do, or can not well do, for themselves, fall into two classes: those which have relation to wrongs, and those which have not. Each of these branch off into an infinite variety of subdivisions. The firstthat in relation to wrongsembraces all crimes, misdemeanors, and nonperformance of contracts. The other embraces all which, in its nature, and without wrong, requires combined action, as public roads and highways, public schools, charities, pauperism, orphanage, estates of the deceased, and the machinery of government itself. From this it appears that if all men were just, there still would be some, though not so much, need for government.
        """.trimIndent()

    private fun largeSingleLineQuotationReduced() =
        """
            The legitimate object of government, is to do for a community of people, whatever they need to have done, but can not do, at all, or can not, so well do, for themselves in their ...
        """.trimIndent()

    @Test
    fun restrictQuotationSize() {
        assertEquals(
            largeMultiLineQuoationReduced(),
            notificationHelper.restrictQuotationSize(largeMultiLineQuotation())
        )

        assertEquals(
            largeSingleLineQuotationReduced(),
            notificationHelper.restrictQuotationSize(largeSingleLineQuotation())
        )
    }

    @Test
    fun restrictAuthorSize() {
        val veryLongAuthor = "one two three four five six seven eight nine ten"
        assertEquals(
            "one two three four five ...",
            notificationHelper.restrictAuthorSize(veryLongAuthor)
        )
    }
}
