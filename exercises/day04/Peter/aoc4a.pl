
my $totalscore = 0;

my %winNumbers = {};


while (<>) {
	chop();
	my ($card, $winNumStr,$myNumStr) = ($_ =~ m/Card +(\d+): +([ \d]+) +\| +([ \d]+)/);
    %winNumbers = {};
    foreach $num ( split(/ +/, $winNumStr) ) {
        $winNumbers{$num} = 1;
    }
    my $score = 0;
    foreach $num ( split(/ +/, $myNumStr) ) {
        if ($winNumbers{$num}) {
            if ($score) {
                $score *= 2;
            } else {
                $score = 1;
            }
        }
    }
    print "Card $card: $score\n";
	$totalscore += $score;
}

print "\n== $totalscore\n";
