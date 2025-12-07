
my $totalscore = 0;

my @ranges = ();
while (<>) {
	chop();
  my ($rStart,$rEnd) = ($_ =~ m/(\d+)\-(\d+)/);
  $rStart ne "" || last;
  push(@ranges, [$rStart,$rEnd]);
}

while (<>) {
	chop();
  my $id = $_;
  my $fresh = 0;
  for $r ( @ranges ) {
    if ($id >= $r->[0] && $id <= $r->[1]) {
      $totalscore++;
      $fresh++;
      last;
    }
  }
}

print "\n== $totalscore\n";
