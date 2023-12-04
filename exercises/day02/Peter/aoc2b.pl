
my $totalscore = 0;

my %cubeMax = ();


while (<>) {
	chop();
	my ($gameNo, $gameStr) = ($_ =~ m/Game +(\d+): +([ ,;\w]+)/);

    %cubeMax = ( red => 0, green => 0, blue => 0 );

    foreach $game ( split(/; +/, $gameStr) ) {
        foreach $bag ( split(/, +/, $game) ) {
            ($cubeNum, $tag) = split(/ /, $bag);
            ( $cubeNum > $cubeMax{$tag} ) and ( $cubeMax{$tag} = $cubeNum );
        }
    }
    print "Game $gameNo: $cubeMax{red} $cubeMax{green} $cubeMax{blue}\n";
    $totalscore += $cubeMax{red} * $cubeMax{green} * $cubeMax{blue};
}

print "\n== $totalscore\n";
