
my $totalscore = 0;

my @ranges = ();
while (<>) {
	chop();
  my ($rStart,$rEnd) = ($_ =~ m/(\d+)\-(\d+)/);
  $rStart ne "" || last;
  push(@ranges, [$rStart,$rEnd]);
}

my @freshRanges = ();

for my $r ( @ranges ) {
#    print ":: $r->[0] - $r->[1] \n";
  for $fr ( @freshRanges ) {    
    if ($r->[0] >= $fr->[0] && $r->[1] <= $fr->[1]) { # fully included
#    print "f: $r->[0] - $r->[1] / $fr->[0] - $fr->[1]\n";
      $r->[1] = $r->[0]-1;
      last;
    } elsif ($r->[0] < $fr->[0] && $r->[1] > $fr->[1]) { # overlapping fully
#    print "o: $r->[0] - $r->[1] / $fr->[0] - $fr->[1]\n";
      push(@ranges, [$fr->[1]+1,$r->[1]]);
      $r->[1] = $fr->[0]-1;
#    print "r: $r->[0] - $r->[1] / $fr->[0] - $fr->[1]\n";
    } elsif ($r->[0] >= $fr->[0] && $r->[0] <= $fr->[1]) { # overlapping right
      $r->[0] = $fr->[1]+1;
#    print "r: $r->[0] - $r->[1] / $fr->[0] - $fr->[1]\n";
    } elsif ($r->[1] <= $fr->[1] && $r->[1] >= $fr->[0]) { # overlapping left
      $r->[1] = $fr->[0]-1;
#    print "l: $r->[0] - $r->[1] / $fr->[0] - $fr->[1]\n";
    }
  }
  if ($r->[1] >= $r->[0]) {
    push(@freshRanges, $r);
    print "$r->[0] - $r->[1]\n";
    $totalscore += $r->[1] - $r->[0] + 1;
  }
}

print "\n== $totalscore\n";
