
my $totalscore = 0;

my @seeds = ();
my @maps = ();
my $mapNum = -1;

while (<>) {
	chop();
    if ( $_ =~ /seeds: (\d[ \d]+\d)/ ) {
        @seeds = split(/ /, $1);
    } elsif ($_ =~ /([-\w]+) map/) {
        $mapNum++;
    } elsif ($_ =~ /(\d[ \d]+\d)/ ) {
        push( @{$maps[$mapNum]}, [ split(/ /, $1) ] );
    } 
}

foreach my $s ( @seeds ) {
    for (my $m=0 ; $m<=$mapNum ; $m++) {
        foreach my $map ( @{$maps[$m]} ) {
            if ( $s >= ${$map}[1] and $s < (${$map}[1] + ${$map}[2])) {
                print "$s -> ";
                $s += ${$map}[0] - ${$map}[1];
                print "$s\n";
                last; 
            } else {
#                print "$s: ${$map}[1] ".(${$map}[1] + ${$map}[2])."\n";
            }
        } 
    }
    if (!$totalscore or $totalscore > $s) {
        $totalscore = $s;
    }
    print "\n";
}

print "\n== $totalscore\n";
