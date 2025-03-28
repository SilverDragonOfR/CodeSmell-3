public class LocaleComparator extends CollatorComparator
{
    /**
     * Default constructor uses the current locale's collator.
     */
    public LocaleComparator()
    {
        m_collator = Collator.getInstance();
    }

    /**
     * use a specific locale's collator.
     */
    public LocaleComparator( Locale locale )
    {
        m_collator = Collator.getInstance( locale );
    }

    /**
     * Specify a new locale.
     * 
     * @param locale the locale for future comparisons
     */
    public void setLocale( Locale locale)
    {
        m_collator = Collator.getInstance(locale);
    }
}