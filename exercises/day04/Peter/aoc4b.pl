
my $totalscore = 0;

my %winNumbers = {};
my @cardCopies = ();

while (<>) {
	chop();
	my ($card, $winNumStr,$myNumStr) = ($_ =~ m/Card +(\d+): +([ \d]+) +\| +([ \d]+)/);
    %winNumbers = {};
    foreach $num ( split(/ +/, $winNumStr) ) {
        $winNumbers{$num} = 1;
    }
    my $matches = 0;
    foreach $num ( split(/ +/, $myNumStr) ) {
        if ($winNumbers{$num}) {
            $matches++;
        }
    }
    print "Card $card (".$cardCopies[$card].") $matches\n";
    for (my $c=0 ; $c<$matches ; $c++) {
        $cardCopies[$card+1+$c] += $cardCopies[$card] + 1;
    }

	$totalscore += $cardCopies[$card] + 1;
}

print "\n== $totalscore\n";
