
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

for (my $l=0 ; $l <= $#seeds ; $l+=2) {
  for (my $m=$seeds[$l] ; $m< $seeds[$l]+$seeds[$l+1] ; $m++) {
    my $r=0;
    my $s = $m;
print "$s:\n";
    for (my $m=0 ; $m<=$mapNum ; $m++) {
        foreach my $map ( @{$maps[$m]} ) {
            if ( $s >= ${$map}[1] and $s < (${$map}[1] + ${$map}[2])) {
                print "$s -> ";
                if (!$r or $r > (${$map}[1] + ${$map}[2] - $s)) {
                    $r = ${$map}[1] + ${$map}[2] - $s -1;
                }
                $s += ${$map}[0] - ${$map}[1];
                print "$s ($r)\n";
                $r>0 or exit;
                last; 
            } else {
#                print "$s: ${$map}[1] ".(${$map}[1] + ${$map}[2])."\n";
            }
        } 
    }
    if (!$totalscore or $totalscore > $s) {
        $totalscore = $s;
    }
    $m += $r;
#    print "\n";
  }
}

print "\n== $totalscore\n";
